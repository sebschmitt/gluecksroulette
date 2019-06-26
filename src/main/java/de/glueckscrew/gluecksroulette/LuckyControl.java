package de.glueckscrew.gluecksroulette;

import com.sun.javafx.PlatformUtil;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.User32;
import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.gui.LuckyGUI;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKeyHandler;
import de.glueckscrew.gluecksroulette.io.LuckyIO;
import de.glueckscrew.gluecksroulette.models.LuckyCourse;
import de.glueckscrew.gluecksroulette.models.LuckyStudent;
import de.glueckscrew.gluecksroulette.physics.LuckyPhysics;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayground;
import de.glueckscrew.gluecksroulette.playground.LuckyPlaygroundListener;
import de.glueckscrew.gluecksroulette.util.LuckyFileUtil;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This class represents the central control component
 * It also contains the main method
 *
 * @author Sebastian Schmitt
 */
public class LuckyControl extends Application implements LuckyPlaygroundListener {
    private static final Logger LOGGER = Logger.getLogger(LuckyControl.class.getSimpleName());

    private Scene mainScene;
    private Stage primaryStage;
    private LuckyPlayground playground;
    private LuckyGUI gui;
    private LuckyConfig config;
    private File lastCourseFile;
    private Timer focusChangeTimer;

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("start");

        this.primaryStage = primaryStage;
        config = new LuckyConfig();

        if (getParameters().getRaw().size() > 0) {
            lastCourseFile = new File(getParameters().getRaw().get(0));
        } else {
            if (!config.getString(LuckyConfig.Key.LAST_COURSE).isEmpty())
                lastCourseFile = new File(config.getString(LuckyConfig.Key.LAST_COURSE));
        }
        LuckyCourse course = null;
        if (lastCourseFile != null)
            course = LuckyFileUtil.loadCourse(lastCourseFile, LOGGER);

        if (course != null) {
            config.set(LuckyConfig.Key.LAST_COURSE, lastCourseFile.getAbsolutePath());
        } else  {
            lastCourseFile = null;
        }

        playground = new LuckyPlayground(config, course);
        playground.setListener(this);
        gui = new LuckyGUI(playground, config);

        mainScene = new Scene(gui, config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT));
        primaryStage.setScene(mainScene);

        playground.heightProperty().bind(mainScene.heightProperty());
        playground.widthProperty().bind(mainScene.widthProperty());

        /*
         * Move camera on resize so wheel is always centered
         */
        Camera camera = playground.getCamera();
        mainScene.widthProperty().addListener((obs, oldVal, newVal) -> {
            config.set(LuckyConfig.Key.WINDOW_WIDTH, newVal.intValue());
            gui.update();

            camera.setTranslateX(camera.getTranslateX() + (oldVal.doubleValue() - newVal.doubleValue()) / 2);
            camera.setTranslateZ(camera.getTranslateZ() - (oldVal.doubleValue() - newVal.doubleValue()) / 4);

            config.set(LuckyConfig.Key.CAMERA_X, camera.getTranslateX());
            config.set(LuckyConfig.Key.CAMERA_Z, camera.getTranslateZ());
        });

        mainScene.heightProperty().addListener((obs, oldVal, newVal) -> {
            config.set(LuckyConfig.Key.WINDOW_HEIGHT, newVal.intValue());
            gui.update();

            camera.setTranslateY(camera.getTranslateY() + (oldVal.doubleValue() - newVal.doubleValue()) / 2);
            camera.setTranslateZ(camera.getTranslateZ() - (oldVal.doubleValue() - newVal.doubleValue()) / 4);

            config.set(LuckyConfig.Key.CAMERA_Y, camera.getTranslateY());
            config.set(LuckyConfig.Key.CAMERA_Z, camera.getTranslateZ());
        });


        registerHotKeys();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        config.save();
        LOGGER.info("Good Bye!");
    }

    private void registerHotKeys() {
        LuckyHotKeyHandler hotKeyHandler = new LuckyHotKeyHandler();

        mainScene.setOnKeyPressed(hotKeyHandler.keyPressed());
        mainScene.setOnKeyReleased(hotKeyHandler.keyReleased());

        if (Platform.isWindows())
            hotKeyHandler.registerAny(() -> {
                if (focusChangeTimer != null)
                    focusChangeTimer.cancel();
            });

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_TOGGLE_HELP), gui::toggleHints);
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_SPIN), () -> {
            gui.fadeOutSelectedStudent();
            playground.spin();
        });

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_HARD_RESET), () -> {
            if (LuckyPhysics.getInstance().isSpinning()) return;

            playground.hardReset();
            saveCourseFile();
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_SOFT_RESET), () -> {
            playground.softReset();
            saveCourseFile();
        });

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_REDUCE), () -> {
            if (LuckyPhysics.getInstance().isSpinning()) return;

            if (playground.reduceSelected(false, config.getInt(LuckyConfig.Key.MANUAL_WEIGHT_CHANGE)))
                saveCourseFile();
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_ENLARGE), () -> {
            if (LuckyPhysics.getInstance().isSpinning()) return;

            if (playground.enlargeSelected(false, config.getInt(LuckyConfig.Key.MANUAL_WEIGHT_CHANGE)))
                saveCourseFile();
         });

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_TOGGLE_MODE),  () -> {
            if (config.getMode(LuckyConfig.Key.MODE) == LuckyMode.CONSERVING)
                config.set(LuckyConfig.Key.MODE, LuckyMode.THINNING);
            else
                config.set(LuckyConfig.Key.MODE, LuckyMode.CONSERVING);

            gui.updateMode();
        });

        if (PlatformUtil.isWindows())
            hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_FOCUS_CHANGE_TOGGLE), () -> {
                        config.set(LuckyConfig.Key.FOCUS_CHANGE_ACTIVE, !config.getBool(LuckyConfig.Key.FOCUS_CHANGE_ACTIVE));
                        gui.updateFocusChange();
                    }
            );

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_OPEN_COURSE_FILE), () -> {
            if (LuckyPhysics.getInstance().isSpinning()) return;

            FileChooser fileChooser = new FileChooser();
            if (lastCourseFile != null)
                fileChooser.setInitialDirectory(lastCourseFile.getParentFile());
            fileChooser.setTitle("Open Course File");
            lastCourseFile = fileChooser.showOpenDialog(primaryStage);

            if (lastCourseFile != null) {
                LuckyCourse course = LuckyFileUtil.loadCourse(lastCourseFile, LOGGER);
                if (course != null) {
                    playground.setCurrentCourse(course);
                    primaryStage.setTitle(course.getIdentifier());
                    config.set(LuckyConfig.Key.LAST_COURSE, lastCourseFile.getAbsolutePath());
                }
            }
        });


        // make sure that all key states are released on focus lost
        primaryStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) hotKeyHandler.releaseAll();
        });

        Camera camera = playground.getCamera();
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_TURN_UP), () -> {
            Rotate rotate = (Rotate) camera.getTransforms().get(LuckyPlayground.CAMERA_ROT_X);
            rotate.setAngle(rotate.getAngle() - config.getDouble(LuckyConfig.Key.CAMERA_ROT_STEP));
            camera.getTransforms().set(LuckyPlayground.CAMERA_ROT_X, rotate);

            config.set(LuckyConfig.Key.CAMERA_ROT_X, rotate.getAngle());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_TURN_DOWN), () -> {
            Rotate rotate = (Rotate) camera.getTransforms().get(LuckyPlayground.CAMERA_ROT_X);
            rotate.setAngle(rotate.getAngle() + config.getDouble(LuckyConfig.Key.CAMERA_ROT_STEP));
            camera.getTransforms().set(LuckyPlayground.CAMERA_ROT_X, rotate);

            config.set(LuckyConfig.Key.CAMERA_ROT_X, rotate.getAngle());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_TURN_LEFT), () -> {
            Rotate rotate = (Rotate) camera.getTransforms().get(LuckyPlayground.CAMERA_ROT_Y);
            rotate.setAngle(rotate.getAngle() + config.getDouble(LuckyConfig.Key.CAMERA_ROT_STEP));
            camera.getTransforms().set(LuckyPlayground.CAMERA_ROT_Y, rotate);

            config.set(LuckyConfig.Key.CAMERA_ROT_Y, rotate.getAngle());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_TURN_RIGHT), () -> {
            Rotate rotate = (Rotate) camera.getTransforms().get(LuckyPlayground.CAMERA_ROT_Y);
            rotate.setAngle(rotate.getAngle() - config.getDouble(LuckyConfig.Key.CAMERA_ROT_STEP));
            camera.getTransforms().set(LuckyPlayground.CAMERA_ROT_Y, rotate);

            config.set(LuckyConfig.Key.CAMERA_ROT_Y, rotate.getAngle());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_UP), () -> {
            camera.setTranslateY(camera.getTranslateY() - config.getDouble(LuckyConfig.Key.CAMERA_MOVE_STEP));
            config.set(LuckyConfig.Key.CAMERA_Y, camera.getTranslateY());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_DOWN), () -> {
            camera.setTranslateY(camera.getTranslateY() + config.getDouble(LuckyConfig.Key.CAMERA_MOVE_STEP));
            config.set(LuckyConfig.Key.CAMERA_Y, camera.getTranslateY());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_LEFT), () -> {
            camera.setTranslateX(camera.getTranslateX() - config.getDouble(LuckyConfig.Key.CAMERA_MOVE_STEP));
            config.set(LuckyConfig.Key.CAMERA_X, camera.getTranslateX());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_RIGHT), () -> {
            camera.setTranslateX(camera.getTranslateX() + config.getDouble(LuckyConfig.Key.CAMERA_MOVE_STEP));
            config.set(LuckyConfig.Key.CAMERA_X, camera.getTranslateX());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_FORWARD), () -> {
            camera.setTranslateZ(camera.getTranslateZ() + config.getDouble(LuckyConfig.Key.CAMERA_MOVE_STEP));
            config.set(LuckyConfig.Key.CAMERA_Z, camera.getTranslateZ());
        });
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_CAMERA_BACKWARD), () -> {
            camera.setTranslateZ(camera.getTranslateZ() - config.getDouble(LuckyConfig.Key.CAMERA_MOVE_STEP));
            config.set(LuckyConfig.Key.CAMERA_Z, camera.getTranslateZ());
        });
    }

    private void saveCourseFile() {
        if (lastCourseFile == null) return;
        LOGGER.info("Coursefile autosave");
        config.set(LuckyConfig.Key.LAST_COURSE, lastCourseFile.getAbsolutePath());
        LuckyIO.write(lastCourseFile, playground.getCurrentCourse().serialize());
    }


    @Override
    public void onSpinStop() {
        saveCourseFile();
        LuckyStudent student = playground.getSelectedStudent();
        if (student != null)
            gui.showSelectedStudent(student);


        if (PlatformUtil.isWindows() && config.getBool(LuckyConfig.Key.FOCUS_CHANGE_ACTIVE)) {
            focusChangeTimer = new Timer();
            focusChangeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    User32.INSTANCE.EnumWindows((hWnd, userData) -> {
                        char[] windowText = new char[512];
                        User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
                        String wText = Native.toString(windowText);

                        if (wText.toLowerCase().contains(config.getString(LuckyConfig.Key.FOCUS_CHANGE_WINDOW_NAME))) {
                            User32.INSTANCE.SetForegroundWindow(hWnd);
                            return false;
                        }
                        return true;
                    }, null);
                }
            }, TimeUnit.SECONDS.toMillis(config.getInt(LuckyConfig.Key.FOCUS_CHANGE_TIMEOUT_SECONDS)));
        }
    }

    @Override
    public void onCourseNameChanged() {
        primaryStage.setTitle(playground.getCurrentCourse().getIdentifier());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
