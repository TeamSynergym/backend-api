package org.synergym.backendapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry regi){
    regi.addMapping("/**")
            .allowedOrigins(
              "http://localhost:5173", 
              "http://localhost:5174", 
              "http://localhost:5175",

              "http://10.0.2.2:5173",
              "http://10.0.2.2:5174",
              "http://10.0.2.2:5175",

              "http://192.168.2.6:5173",
              "http://192.168.2.6:5174",
              "http://192.168.2.6:5175",

              "http://192.168.2.168:5173",
              "http://192.168.2.168:5174",
              "http://192.168.2.168:5175",
              
              // nginx
              "http://192.168.2.168:8080",

              // CORS 매번 바뀐다.
              "https://somerset-teacher-golden-pale.trycloudflare.com"
              )
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS","PATCH")
            .allowedHeaders("*")
            .allowCredentials(true);
  }

}
