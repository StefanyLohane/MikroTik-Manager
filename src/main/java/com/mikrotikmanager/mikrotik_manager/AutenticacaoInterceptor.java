
package com.mikrotikmanager.mikrotik_manager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String caminho = request.getRequestURI()
                .substring(request.getContextPath().length());

        if (caminho.equals("/")
                || caminho.equals("/inde.html")
                || caminho.equals("/api/auth/login")
                || caminho.startsWith("/css/")
                || caminho.startsWith("/js/")
                || caminho.startsWith("/img/")
                || caminho.equals("/favicon.ico")) {
            return true;
        }

        HttpSession sessao = request.getSession(false);

        boolean autenticado = sessao != null
                && sessao.getAttribute("mikrotikEndereco") != null
                && sessao.getAttribute("mikrotikUsuario") != null
                && sessao.getAttribute("mikrotikSenha") != null;

        if (autenticado) {
            return true;
        }

        if (caminho.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"erro\":\"Faça login para continuar.\"}"
            );
        } else {
            response.sendRedirect(
                    request.getContextPath() + "/inde.html"
            );
        }

        return false;
    }
}
