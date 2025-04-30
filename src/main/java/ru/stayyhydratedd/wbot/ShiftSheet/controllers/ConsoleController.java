package ru.stayyhydratedd.wbot.ShiftSheet.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import ru.stayyhydratedd.wbot.ShiftSheet.context.ConditionContextManager;
import ru.stayyhydratedd.wbot.ShiftSheet.context.SessionContext;
import ru.stayyhydratedd.wbot.ShiftSheet.dtos.AuthUserDTO;
import ru.stayyhydratedd.wbot.ShiftSheet.models.User;
import ru.stayyhydratedd.wbot.ShiftSheet.services.UserService;
import ru.stayyhydratedd.wbot.ShiftSheet.util.HelperUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.InputOutputUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.JColorUtil;
import ru.stayyhydratedd.wbot.ShiftSheet.util.PrinterUtil;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ConsoleController {

    private final AuthenticationManager authenticationManager;
    private final ConditionContextManager contextManager;
    private final SessionContext sessionContext;
    private final UserService userService;
    private final JColorUtil jColorUtil;
    private final InputOutputUtil inputUtil;
    private final PrinterUtil printer;
    private final HelperUtil helper;

    public void authenticate(AuthUserDTO authUser) {

        while (true) {
            System.out.printf("%sВведите пароль для входа:\n", jColorUtil.INFO);
            Optional<String> passwordOpt = inputUtil.parseInput();
            authUser.setPassword(passwordOpt.orElseThrow());

            try {
                setAuthentication(authUser);
                System.out.printf("Привет, %s!\n",
                        jColorUtil.turnTextIntoColor(authUser.getUsername(), JColorUtil.COLOR.INFO));
                return;

            } catch (BadCredentialsException e) {
                System.out.printf("%sНеправильный пароль\n", jColorUtil.ERROR);
            }
        }
    }

    public void setAuthentication(AuthUserDTO authUser) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authUser.getUsername(),
                        authUser.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<User> foundUser = userService.findByUsername(authUser.getUsername());
        foundUser.ifPresent(sessionContext::setCurrentUser);
    }
}
