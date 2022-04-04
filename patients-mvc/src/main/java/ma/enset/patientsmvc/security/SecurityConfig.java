package ma.enset.patientsmvc.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration//spring va instancier les classes avec l'annotation Configuration avant tous
@EnableWebSecurity//activer la securiter web
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;

    //on precise comment spring security va cercher les users et les roles
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        PasswordEncoder passwordEncoder=passwordEncoder();

        /*
        //inMemoryAuthentication est tres pratique pour tester les applications
        auth.inMemoryAuthentication()//les users qui ont le droit d'acceder sont stocket dans la memoire
                .withUser("user1").password(passwordEncoder.encode("1234")).roles("USER");
        auth.inMemoryAuthentication().withUser("user2").password(passwordEncoder.encode("2345")).roles("USER");

        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder.encode("1111")).roles("USER","ADMIN");
       */
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("SELECT username as principal,password as credentials,active FROM users WHERE username=?")
                .authoritiesByUsernameQuery("SELECT username as principal,role as role FROM  user_role WHERE username=?")
                .rolePrefix("ROLE_")
                .passwordEncoder(passwordEncoder);
        }

    @Override//pour spicifier les droits d'acces
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin();
        http.authorizeHttpRequests().antMatchers("/").permitAll();
        http.authorizeHttpRequests().antMatchers("/admin/**").hasRole("ADMIN");
        http.authorizeHttpRequests().antMatchers("/user/**").hasAnyRole("USER");
        http.authorizeHttpRequests().anyRequest().authenticated();//tous les requetes nessecite une authentification
        http.exceptionHandling().accessDeniedPage("/403");

    }
    @Bean //au demarage cree moi un objet PasswordEncoder et place dans ton contexte
    PasswordEncoder passwordEncoder(){
        return  new BCryptPasswordEncoder();
    }

}
