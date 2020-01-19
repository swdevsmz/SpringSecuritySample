package com.example.demo.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * Web全般のセキュリティ
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("resources/**");
    }

    /**
     * Http通信のセキュリティ
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // フォーム認証の場合
        // http.authorizeRequests() // 対象リクエストの指定
        // .mvcMatchers("/hello").permitAll() // 認証外URL（常にアクセス許可）
        // .anyRequest() // すべてのリクエストを要認証（上記URLのぞく）
        // .authenticated().and().formLogin() // フォーム認証の設定
        // .defaultSuccessUrl("/success"); // ログイン成功後のページ

        HeaderCheckFilter filter = new HeaderCheckFilter("X-Token");

        filter.setAuthenticationManager(new MyAuthenticationManager());

        // 認証の対象となるパス
        http.antMatcher("/*");
        // フィルタの設定
        http.addFilter(filter);

        // 対象のすべてのパスに対して認証を有効にする
        http.authorizeRequests().anyRequest().authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.csrf().disable();

        http.exceptionHandling().authenticationEntryPoint(new ErrorAuthEntryPoint());

    }

    /**
     * 認証方法の実装を行う
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    }

    public class HeaderCheckFilter extends AbstractPreAuthenticatedProcessingFilter {

        private String headerName;

        public HeaderCheckFilter(String headerName) {
            this.headerName = headerName;
        }

        // (2) ヘッダ(X-Token)の値を返す
        @Override
        protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
            return request.getHeader(headerName);
        }

        @Override
        protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
            return "";
        }
    }

    public class MyAuthenticationManager implements AuthenticationManager {

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {

            String principal = (String) authentication.getPrincipal();

            // X-Token の値をチェックする
            if (principal.equals("PASS")) {
                authentication.setAuthenticated(true);
            } else {
                throw new BadCredentialsException("Token key error");
            }
            return authentication;
        }
    }

    public class ErrorAuthEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                throws IOException, ServletException {

            // (3) 実際のエラーを出力する
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            mapper.writeValue(response.getWriter(), "エラー");

        }
    }
}