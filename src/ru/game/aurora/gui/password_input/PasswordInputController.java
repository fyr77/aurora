package ru.game.aurora.gui.password_input;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;
import de.lessvoid.nifty.controls.textfield.format.FormatPassword;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.newdawn.slick.GameContainer;
import ru.game.aurora.gui.GUI;
import ru.game.aurora.world.World;

/**
 * Created by di Grigio on 02.03.2017.
 * Login-Password input window
 * Use PasswordInputEventsController to set custom input processing:
 * - onClose event
 * - onInput event (to show possible input errors)
 * - onLogin event (to check login and password on try of login)
 */
public class PasswordInputController implements ScreenController {

    private final World world;
    private final GameContainer gameContainer;

    private TextField loginInputTextField;
    private TextField passwordInputTextField;
    private Label errorLabel;

    private String currentText;

    private PasswordInputEventsController eventsController;

    private boolean loginEnabled;
    private String defaultLogin;

    public PasswordInputController(World world, GameContainer gameContainer) {
        this.world = world;
        this.gameContainer = gameContainer;
    }

    public void setEventObserver(PasswordInputEventsController checker){
        this.eventsController = checker;
    }

    public void setLoginField(String login) {
        this.defaultLogin = login;
    }

    public void setLoginFieldEnabled(boolean value){
        this.loginEnabled = value;
    }

    public void onClose() {
        GUI.getInstance().popAndSetScreen();
        if(eventsController != null){
            eventsController.onClose();
        }
    }

    public void onLogin() {
        if(eventsController != null && currentText != null){
            eventsController.onLogin(currentText, errorLabel);
        }
    }

    @NiftyEventSubscriber(id = "password_textfield")
    public void onPasswordChanged(final String id, final TextFieldChangedEvent event){
        currentText = event.getText();
        if(eventsController != null && currentText != null) {
            eventsController.onPasswordChanged(currentText, errorLabel);
        }
    }

    @NiftyEventSubscriber(id = "password_view_checkbox")
    public void onViewChanged(final String id, final CheckBoxStateChangedEvent event){
        if(event.isChecked()){
            passwordInputTextField.setFormat(null); // show password
        }
        else{
            passwordInputTextField.setFormat(new FormatPassword()); // hide password
        }

        // hack: update textfield after checkbox checked/unchecked
        passwordInputTextField.setFocus();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        errorLabel = screen.findNiftyControl("error_label", Label.class);
        loginInputTextField = screen.findNiftyControl("login_textfield", TextField.class);
        passwordInputTextField = screen.findNiftyControl("password_textfield", TextField.class);
        passwordInputTextField.setFormat(new FormatPassword());
    }

    @Override
    public void onStartScreen() {
        if(defaultLogin != null){
            loginInputTextField.setText(defaultLogin);
            defaultLogin = null;
        }

        passwordInputTextField.setText(""); // clear password

        if(loginEnabled){
            loginInputTextField.enable();
        }
        else{
            loginInputTextField.disable();
        }

        gameContainer.getInput().disableKeyRepeat();
        world.setPaused(true);
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(false);
    }

    @Override
    public void onEndScreen() {
        gameContainer.getInput().enableKeyRepeat();
        world.setPaused(false);
        GUI.getInstance().getNifty().setIgnoreKeyboardEvents(true);
    }
}
