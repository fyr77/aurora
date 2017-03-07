package ru.game.aurora.gui.password_input;

import de.lessvoid.nifty.controls.Label;

/**
 * Created by di Grigio on 02.03.2017.
 * Use this class to set custom input events from PasswordInputController.class
 */
public abstract class PasswordInputEventsController {

    public abstract void onPasswordChanged(String passwordInputed, Label errorLabel);
    public abstract void onLogin(String passwordInputed, Label errorLabel);
    public abstract void onClose();
}