package com.tlcsdm.insightpc.config;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import com.tlcsdm.insightpc.model.DisplayLocale;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Application settings management using PreferencesFX.
 */
public class AppSettings {

    private static final Logger LOG = LoggerFactory.getLogger(AppSettings.class);

    private static AppSettings instance;

    private final ObjectProperty<DisplayLocale> languageProperty;
    private final ObjectProperty<AppTheme> themeProperty;

    private PreferencesFx preferencesFx;
    private boolean suppressRebuild;

    private AppSettings() {
        languageProperty = new SimpleObjectProperty<>(new DisplayLocale(I18N.getCurrentLocale()));
        themeProperty = new SimpleObjectProperty<>(AppTheme.getSavedTheme());

        languageProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal) && !suppressRebuild) {
                I18N.setLocale(newVal.getLocale());
                rebuildPreferences();
            }
        });

        themeProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                newVal.apply();
                AppTheme.saveTheme(newVal);
            }
        });
    }

    /**
     * Get the singleton instance.
     *
     * @return shared settings manager
     */
    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    /**
     * Get the language property.
     *
     * @return selected display locale property
     */
    public ObjectProperty<DisplayLocale> languageProperty() {
        return languageProperty;
    }

    /**
     * Get the theme property.
     *
     * @return selected application theme property
     */
    public ObjectProperty<AppTheme> themeProperty() {
        return themeProperty;
    }

    /**
     * Create and get the PreferencesFx instance.
     *
     * @return configured PreferencesFX dialog model
     */
    public PreferencesFx getPreferencesFx() {
        if (preferencesFx == null) {
            buildPreferences();
        }
        return preferencesFx;
    }

    private void buildPreferences() {
        DisplayLocale savedLocale = new DisplayLocale(I18N.getCurrentLocale());
        List<DisplayLocale> supportedLocales = Arrays.stream(I18N.getSupportedLocales())
            .map(DisplayLocale::new)
            .toList();
        List<AppTheme> themes = Arrays.asList(AppTheme.values());

        suppressRebuild = true;
        try {
            preferencesFx = PreferencesFx.of(AppSettings.class,
                Category.of(I18N.get("settings.general"),
                    Group.of(I18N.get("settings.languageAndTheme"),
                        Setting.of(I18N.get("settings.language"),
                            FXCollections.observableArrayList(supportedLocales),
                            languageProperty),
                        Setting.of(I18N.get("settings.theme"),
                            FXCollections.observableArrayList(themes),
                            themeProperty)
                    )
                )
            ).persistWindowState(false)
                .saveSettings(true)
                .debugHistoryMode(false)
                .buttonsVisibility(false)
                .instantPersistent(true)
                .dialogTitle(I18N.get("settings.title"));

            Image logo = loadLogoImage();
            if (logo != null) {
                preferencesFx.dialogIcon(logo);
            }

            if (!savedLocale.equals(languageProperty.get())) {
                languageProperty.set(savedLocale);
            }
        } finally {
            suppressRebuild = false;
        }
    }

    private void rebuildPreferences() {
        preferencesFx = null;
    }

    private Image loadLogoImage() {
        try {
            return new Image(AppSettings.class.getResourceAsStream("/com/tlcsdm/insightpc/logo.png"));
        } catch (Exception e) {
            LOG.warn("Could not set preferences dialog icon", e);
            return null;
        }
    }

    /**
     * Apply initial settings (called at application startup).
     */
    public void applyInitialSettings() {
        AppTheme.applySavedTheme();
    }
}
