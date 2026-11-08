package com.elouage.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String h=request.getHeader("Authorization");
        if(h!=null && h.startsWith("Bearer ")){
            try{
                Claims c=jwtService.lire(h.substring(7));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(Long.valueOf(c.getSubject()),null,
                                List.of(new SimpleGrantedAuthority("ROLE_"+c.get("role", String.class)))));
            }
            catch (JwtException | IllegalArgumentException ignored){
                //invalide or exprirée 401 stays anonymous
            }
        }
        filterChain.doFilter(request,response);
    }
}
