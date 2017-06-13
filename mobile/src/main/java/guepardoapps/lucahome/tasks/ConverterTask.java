package guepardoapps.lucahome.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.Locale;

import guepardoapps.library.lucahome.common.constants.Broadcasts;
import guepardoapps.library.lucahome.common.constants.Bundles;
import guepardoapps.library.lucahome.common.constants.SharedPrefConstants;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.dto.ChangeDto;
import guepardoapps.library.lucahome.common.dto.InformationDto;
import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.dto.MapContentDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.MotionCameraDto;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.controller.DatabaseController;
import guepardoapps.library.lucahome.converter.json.JsonDataToBirthdayConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToChangeConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToInformationConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToListedMenuConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToMapContentConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToMenuConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToMotionCameraDtoConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToMovieConverter;
import guepardoapps.library.lucahome.converter.json.JsonDataToShoppingListConverter;

import guepardoapps.library.toolset.common.Logger;
import guepardoapps.library.toolset.common.classes.SerializableDate;
import guepardoapps.library.toolset.common.classes.SerializableList;
import guepardoapps.library.toolset.common.classes.SerializableTime;
import guepardoapps.library.toolset.controller.BroadcastController;
import guepardoapps.library.toolset.controller.SharedPrefController;

public class ConverterTask extends AsyncTask<String, Void, String> {

    private static final String TAG = ConverterTask.class.getSimpleName();
    private Logger _logger;

    private String[] _jsonStringArray;
    private String _bundle;
    private SerializableList<WirelessSocketDto> _socketList;

    private BroadcastController _broadcastController;
    private DatabaseController _databaseController;
    private SharedPrefController _sharedPrefController;

    public ConverterTask(
            @NonNull String[] jsonStringArray,
            @NonNull String bundle,
            SerializableList<WirelessSocketDto> socketList,
            @NonNull BroadcastController broadcastController,
            @NonNull DatabaseController databaseController,
            @NonNull SharedPrefController sharedPrefController) {
        _logger = new Logger(TAG);

        _jsonStringArray = jsonStringArray;
        _bundle = bundle;
        _socketList = socketList;

        _broadcastController = broadcastController;
        _databaseController = databaseController;
        _sharedPrefController = sharedPrefController;
    }

    @Override
    protected String doInBackground(String... input) {
        _logger.Debug("executing Task");

        try {
            switch (_bundle) {
                case Bundles.BIRTHDAY_LIST:
                    convertBirthdays();
                    break;
                case Bundles.CHANGE_LIST:
                    convertChanges();
                    break;
                case Bundles.INFORMATION_SINGLE:
                    convertInformation();
                    break;
                case Bundles.MAP_CONTENT_LIST:
                    convertMapContent();
                    break;
                case Bundles.LISTED_MENU:
                    convertListedMenu();
                    break;
                case Bundles.MENU:
                    convertMenu();
                    break;
                case Bundles.MOTION_CAMERA_DTO:
                    convertMotionCamera();
                    break;
                case Bundles.MOVIE_LIST:
                    convertMovies();
                    break;
                case Bundles.SHOPPING_LIST:
                    convertShopping();
                    break;

                default:
                    _logger.Error(String.format(Locale.getDefault(), "Data for %s currently not supported!", _bundle));
                    break;
            }

        } catch (Exception exception) {
            _logger.Error(exception.toString());
        }

        return "ConverterTask performed...";
    }

    @Override
    protected void onPostExecute(String result) {
        _logger.Info(String.format(Locale.getDefault(), "onPostExecute result: %s", result));
        super.onPostExecute(result);
    }

    private void convertBirthdays() {
        _logger.Debug("convertBirthdays");

        SerializableList<BirthdayDto> newBirthdayList = JsonDataToBirthdayConverter.GetList(_jsonStringArray);
        if (newBirthdayList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_BIRTHDAY_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newBirthdayList});

            _databaseController.ClearDatabaseBirthday();
            for (int index = 0; index < newBirthdayList.getSize(); index++) {
                _databaseController.SaveBirthday(newBirthdayList.getValue(index));
            }

            SerializableTime time = new SerializableTime();
            SerializableDate date = new SerializableDate();
            _sharedPrefController.SaveStringValue(
                    SharedPrefConstants.LAST_LOADED_BIRTHDAY_TIME,
                    String.format(Locale.getDefault(), "%s-%s-%s-%s-%s", time.HH(), time.MM(), date.DD(), date.MM(), date.YYYY()));
        } else {
            _logger.Warn("newBirthdayList is null");
        }
    }

    private void convertChanges() {
        _logger.Debug("convertChanges");

        SerializableList<ChangeDto> newChangeList = JsonDataToChangeConverter.GetList(_jsonStringArray);
        if (newChangeList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_CHANGE_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newChangeList});

            _databaseController.ClearDatabaseChange();
            for (int index = 0; index < newChangeList.getSize(); index++) {
                _databaseController.SaveChange(newChangeList.getValue(index));
            }
        } else {
            _logger.Warn("newChangeList is null");
        }
    }

    private void convertInformation() {
        _logger.Debug("convertInformation");

        InformationDto newInformation = JsonDataToInformationConverter.Get(_jsonStringArray);
        if (newInformation != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_INFORMATION_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newInformation});
        } else {
            _logger.Warn("newInformation is null");
        }
    }

    private void convertMapContent() {
        _logger.Debug("convertMapContent");

        SerializableList<MapContentDto> newMapContentList = JsonDataToMapContentConverter.GetList(_jsonStringArray);
        if (newMapContentList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_MAP_CONTENT_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newMapContentList});

            _databaseController.ClearDatabaseMapContent();
            for (int index = 0; index < newMapContentList.getSize(); index++) {
                _databaseController.SaveMapContent(newMapContentList.getValue(index));
            }

            SerializableTime time = new SerializableTime();
            SerializableDate date = new SerializableDate();
            _sharedPrefController.SaveStringValue(
                    SharedPrefConstants.LAST_LOADED_MAP_CONTENT_TIME,
                    String.format(Locale.getDefault(), "%s-%s-%s-%s-%s", time.HH(), time.MM(), date.DD(), date.MM(), date.YYYY()));
        }
    }

    private void convertListedMenu() {
        _logger.Debug("convertListedMenu");

        SerializableList<ListedMenuDto> newListedMenuList = JsonDataToListedMenuConverter.GetList(_jsonStringArray);
        if (newListedMenuList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_LISTED_MENU_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newListedMenuList});

            _databaseController.ClearDatabaseListedMenu();
            for (int index = 0; index < newListedMenuList.getSize(); index++) {
                _databaseController.SaveListedMenu(newListedMenuList.getValue(index));
            }
        } else {
            _logger.Warn("newListedMenuList is null");
        }
    }

    private void convertMenu() {
        _logger.Debug("convertMenu");

        SerializableList<MenuDto> newMenuList = JsonDataToMenuConverter.GetList(_jsonStringArray);
        if (newMenuList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_MENU_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newMenuList});

            _databaseController.ClearDatabaseMenu();
            for (int index = 0; index < newMenuList.getSize(); index++) {
                _databaseController.SaveMenu(newMenuList.getValue(index));
            }

            SerializableTime time = new SerializableTime();
            SerializableDate date = new SerializableDate();
            _sharedPrefController.SaveStringValue(
                    SharedPrefConstants.LAST_LOADED_MENU_TIME,
                    String.format(Locale.getDefault(), "%s-%s-%s-%s-%s", time.HH(), time.MM(), date.DD(), date.MM(), date.YYYY()));
        } else {
            _logger.Warn("newMenuList is null");
        }
    }

    private void convertMotionCamera() {
        _logger.Debug("convertMotionCamera");

        SerializableList<MotionCameraDto> motionCameraDtoList = JsonDataToMotionCameraDtoConverter.GetList(_jsonStringArray);
        if (motionCameraDtoList != null) {
            if (motionCameraDtoList.getSize() != 1) {
                _logger.Error(String.format(Locale.getDefault(), "MotionCameraDtoList has wrong size %d!", motionCameraDtoList.getSize()));
                return;
            }

            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_MOTION_CAMERA_DTO_FINISHED,
                    new String[]{_bundle},
                    new Object[]{motionCameraDtoList});
        } else {
            _logger.Warn("motionCameraDtoList is null");
        }
    }

    private void convertMovies() {
        _logger.Debug("convertMovies");

        SerializableList<MovieDto> newMovieList = JsonDataToMovieConverter.GetList(_jsonStringArray);
        if (newMovieList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_MOVIE_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newMovieList});

            _databaseController.ClearDatabaseMovie();
            for (int index = 0; index < newMovieList.getSize(); index++) {
                _databaseController.SaveMovie(newMovieList.getValue(index));
            }

            SerializableTime time = new SerializableTime();
            SerializableDate date = new SerializableDate();
            _sharedPrefController.SaveStringValue(
                    SharedPrefConstants.LAST_LOADED_MOVIE_TIME,
                    String.format(Locale.getDefault(), "%s-%s-%s-%s-%s", time.HH(), time.MM(), date.DD(), date.MM(), date.YYYY()));
        }
    }

    private void convertShopping() {
        _logger.Debug("convertShopping");

        SerializableList<ShoppingEntryDto> newShoppingList = JsonDataToShoppingListConverter.GetList(_jsonStringArray);
        if (newShoppingList != null) {
            _broadcastController.SendSerializableArrayBroadcast(
                    Broadcasts.CONVERT_SHOPPING_LIST_FINISHED,
                    new String[]{_bundle},
                    new Object[]{newShoppingList});

            _databaseController.ClearDatabaseShoppingList();
            for (int index = 0; index < newShoppingList.getSize(); index++) {
                _databaseController.SaveShoppingEntry(newShoppingList.getValue(index));
            }

            SerializableTime time = new SerializableTime();
            SerializableDate date = new SerializableDate();
            _sharedPrefController.SaveStringValue(
                    SharedPrefConstants.LAST_LOADED_SHOPPING_LIST_TIME,
                    String.format(Locale.getDefault(), "%s-%s-%s-%s-%s", time.HH(), time.MM(), date.DD(), date.MM(), date.YYYY()));
        } else {
            _logger.Warn("newShoppingList is null");
        }
    }
}