
import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

@Named(value = "twlogin")
@SessionScoped
public class twlogin implements Serializable {

    private String oauthConsumerKey = "-----";
    private String oauthConsumerSecret = "------";
    private ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
    private String oauthVerifier;
    private User user;
    
    
    
    
    private Twitter twitter;
    private RequestToken requestToken;
    public void authorize() throws TwitterException, IOException {
        try {
            //Инициализируем configurationBuilder
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setDebugEnabled(true)
                    //oauthConsumerKey и oauthConsumerSecret это как раз те поля, которые были указанны в настройках созданного нами приложения
                    .setOAuthConsumerKey(oauthConsumerKey)
                    .setOAuthConsumerSecret(oauthConsumerSecret)
                    .setOAuthAccessToken(null)
                    .setOAuthAccessTokenSecret(null);
            //Инициализируем TwitterFactory на основе configurationBuilder
            TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
            //Инициализируем twitter на TwitterFactory
            twitter = tf.getInstance();
            //Теперь подготовка закончена и можно переходить к процессу авторизации
            //requestToken это временный ключ который необходим для запроса секретного ключа доступа
            //Запрос requestToken происходит с помощью метода POST.
            //По сути запрашивая requestToken мы уведомляем сервер твиттера что сейчас запросим секретный ключ доступа пользователя
            //параметром запроса мы указываем тот адрес куда вернётся oauthVerifier
            requestToken = twitter.getOAuthRequestToken("http://localhost:8080/WebApplication1/TwitterLogin.xhtml");
            //после получения requestToken, пользователь перенаправляется на страницу делигации прав секретноиу ключу доступа
            ec.redirect("https://api.twitter.com/oauth/authenticate?oauth_token=" + requestToken.getToken());
        } catch (Exception ex) {
            ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/newjsf.xhtml");
        }
    }

    public void phase2() throws IOException {
        try {
            //Запрашиваем секретный ключ доступа пользователя, в параметры запроса отправляем
            //requestToken и oauthVerifier полученные ранее
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
            //Теперь у нас есть все необходимые значения чтобы заполнить ConfigurationBuilder
            //и получить полный доступ к методам API
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setDebugEnabled(true)
                    .setOAuthConsumerKey(oauthConsumerKey)
                    .setOAuthConsumerSecret(oauthConsumerSecret)
                    .setOAuthAccessToken(accessToken.getToken())
                    .setOAuthAccessTokenSecret(accessToken.getTokenSecret());
            TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
            twitter = tf.getInstance();
            //в экземпляр класса User записываетс исчерпывающая информация для заполнения профайла пользователя
            user = twitter.verifyCredentials();
        } catch (Exception ex) {
            ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath() + "/newjsf.xhtml");
        }
        ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(ec.getRequestContextPath() + "/welcome.xhtml");
    }

    public String getOauthVerifier() {
        return oauthVerifier;
    }

    public void setOauthVerifier(String oauthVerifier) {
        this.oauthVerifier = oauthVerifier;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
