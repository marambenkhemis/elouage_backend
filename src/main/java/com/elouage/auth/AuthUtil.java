package com.elouage.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtil {
    private AuthUtil(){}

    public static void verifier(Long idAttendu){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if(a != null && a.getPrincipal() instanceof Long idToken && idAttendu != null && !idToken.equals(idAttendu)){
            throw new RuntimeException("Vous ne pouvez agir qu'avec votre propre identifiant.");
        }
    }
}
