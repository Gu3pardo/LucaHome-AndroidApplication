package guepardoapps.library.lucahome.controller;

import android.content.Context;
import android.support.annotation.NonNull;

import guepardoapps.library.lucahome.common.dto.ActionDto;
import guepardoapps.library.lucahome.common.dto.BirthdayDto;
import guepardoapps.library.lucahome.common.dto.ChangeDto;
import guepardoapps.library.lucahome.common.dto.ListedMenuDto;
import guepardoapps.library.lucahome.common.dto.MapContentDto;
import guepardoapps.library.lucahome.common.dto.MenuDto;
import guepardoapps.library.lucahome.common.dto.MovieDto;
import guepardoapps.library.lucahome.common.dto.ScheduleDto;
import guepardoapps.library.lucahome.common.dto.ShoppingEntryDto;
import guepardoapps.library.lucahome.common.dto.TimerDto;
import guepardoapps.library.lucahome.common.dto.WirelessSocketDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.database.*;

import guepardoapps.library.toolset.common.classes.SerializableList;

public class DatabaseController {

    private static final DatabaseController SINGLETON = new DatabaseController();

    private static final String TAG = DatabaseController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private boolean _isInitialized;

    private DatabaseActionStore _databaseActionStore;
    private DatabaseBirthdayList _databaseBirthdayList;
    private DatabaseChangeList _databaseChangeList;
    private DatabaseListedMenuList _databaseListedMenuList;
    private DatabaseMapContentList _databaseMapContentList;
    private DatabaseMenuList _databaseMenuList;
    private DatabaseMovieList _databaseMovieList;
    private DatabaseScheduleList _databaseScheduleList;
    private DatabaseShoppingList _databaseShoppingList;
    private DatabaseSocketList _databaseSocketList;
    private DatabaseTimerList _databaseTimerList;

    private DatabaseController() {
        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("DatabaseController created...");
    }

    public static DatabaseController getSingleton() {
        return SINGLETON;
    }

    public void Initialize(@NonNull Context context) {
        _logger.Debug("Initialize");

        if (_isInitialized) {
            _logger.Warn("Already initialized!");
            return;
        }

        _databaseActionStore = new DatabaseActionStore(context);
        _databaseBirthdayList = new DatabaseBirthdayList(context);
        _databaseChangeList = new DatabaseChangeList(context);
        _databaseListedMenuList = new DatabaseListedMenuList(context);
        _databaseMapContentList = new DatabaseMapContentList(context);
        _databaseMenuList = new DatabaseMenuList(context);
        _databaseMovieList = new DatabaseMovieList(context);
        _databaseScheduleList = new DatabaseScheduleList(context);
        _databaseShoppingList = new DatabaseShoppingList(context);
        _databaseSocketList = new DatabaseSocketList(context);
        _databaseTimerList = new DatabaseTimerList(context);

        _databaseActionStore.Open();
        _databaseBirthdayList.Open();
        _databaseChangeList.Open();
        _databaseListedMenuList.Open();
        _databaseMapContentList.Open();
        _databaseMenuList.Open();
        _databaseMovieList.Open();
        _databaseScheduleList.Open();
        _databaseShoppingList.Open();
        _databaseSocketList.Open();
        _databaseTimerList.Open();

        _isInitialized = true;
    }

    public void Dispose() {
        _logger.Debug("Dispose");

        _databaseActionStore.Close();
        _databaseBirthdayList.Close();
        _databaseChangeList.Close();
        _databaseListedMenuList.Close();
        _databaseMapContentList.Close();
        _databaseMenuList.Close();
        _databaseMovieList.Close();
        _databaseScheduleList.Close();
        _databaseShoppingList.Close();
        _databaseSocketList.Close();
        _databaseTimerList.Close();

        _isInitialized = false;
    }

	/*
    Methods for ActionDto
	 */

    public SerializableList<ActionDto> GetActions() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseActionStore.GetStoredActions();
    }

    public boolean SaveAction(@NonNull ActionDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        removeActionNameIfExists(newEntry.GetName());
        _databaseActionStore.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteAction(@NonNull ActionDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseActionStore.Delete(deleteEntry);
        return true;
    }

    public boolean DeleteActionByName(@NonNull String deleteActionName) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        return removeActionNameIfExists(deleteActionName);
    }

    public void ClearDatabaseActions() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<ActionDto> actions = GetActions();
        for (int index = 0; index < actions.getSize(); index++) {
            DeleteAction(actions.getValue(index));
        }
    }

    private boolean removeActionNameIfExists(@NonNull String actionName) {
        SerializableList<ActionDto> savedActions = GetActions();

        if (savedActions != null) {
            for (int index = 0; index < savedActions.getSize(); index++) {
                ActionDto entry = savedActions.getValue(index);
                if (entry != null) {
                    if (entry.GetName().contains(actionName)) {
                        _logger.Info(String.format("Name %s already exists! Removing", entry.GetName()));
                        return DeleteAction(entry);
                    }
                }
            }
        }

        return false;
    }

	/*
    Methods for BirthdayDto
	 */

    public SerializableList<BirthdayDto> GetBirthdayList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseBirthdayList.GetBirthdayList();
    }

    public boolean SaveBirthday(@NonNull BirthdayDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseBirthdayList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteBirthday(@NonNull BirthdayDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseBirthdayList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseBirthday() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<BirthdayDto> birthdayList = GetBirthdayList();
        for (int index = 0; index < birthdayList.getSize(); index++) {
            DeleteBirthday(birthdayList.getValue(index));
        }
    }

	/*
    Methods for ChangeDto
	 */

    public SerializableList<ChangeDto> GetChangeList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseChangeList.GetChangeList();
    }

    public boolean SaveChange(@NonNull ChangeDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseChangeList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteChange(@NonNull ChangeDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseChangeList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseChange() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<ChangeDto> changeList = GetChangeList();
        for (int index = 0; index < changeList.getSize(); index++) {
            DeleteChange(changeList.getValue(index));
        }
    }

	/*
    Methods for ListedMenuDto
	 */

    public SerializableList<ListedMenuDto> GetListedMenuList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseListedMenuList.GetListedMenuList();
    }

    public boolean SaveListedMenu(@NonNull ListedMenuDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseListedMenuList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteListedMenu(@NonNull ListedMenuDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseListedMenuList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseListedMenu() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<ListedMenuDto> listedMenuList = GetListedMenuList();
        for (int index = 0; index < listedMenuList.getSize(); index++) {
            DeleteListedMenu(listedMenuList.getValue(index));
        }
    }

	/*
    Methods for MapContentDto
	 */

    public SerializableList<MapContentDto> GetMapContent() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseMapContentList.GetMapContent();
    }

    public boolean SaveMapContent(@NonNull MapContentDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseMapContentList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteMapContent(@NonNull MapContentDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseMapContentList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseMapContent() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<MapContentDto> mapContent = GetMapContent();
        for (int index = 0; index < mapContent.getSize(); index++) {
            DeleteMapContent(mapContent.getValue(index));
        }
    }

	/*
    Methods for MenuDto
	 */

    public SerializableList<MenuDto> GetMenuList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseMenuList.GetMenuList();
    }

    public boolean SaveMenu(@NonNull MenuDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseMenuList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteMenu(@NonNull MenuDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseMenuList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseMenu() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<MenuDto> menuList = GetMenuList();
        for (int index = 0; index < menuList.getSize(); index++) {
            DeleteMenu(menuList.getValue(index));
        }
    }

	/*
    Methods for MovieDto
	 */

    public SerializableList<MovieDto> GetMovieList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseMovieList.GetMovieList();
    }

    public boolean SaveMovie(@NonNull MovieDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseMovieList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteMovie(@NonNull MovieDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseMovieList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseMovie() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<MovieDto> movieList = GetMovieList();
        for (int index = 0; index < movieList.getSize(); index++) {
            DeleteMovie(movieList.getValue(index));
        }
    }

	/*
    Methods for ScheduleDto
	 */

    public SerializableList<ScheduleDto> GetScheduleList(@NonNull SerializableList<WirelessSocketDto> socketList) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseScheduleList.GetScheduleList(socketList);
    }

    public boolean SaveSchedule(@NonNull ScheduleDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseScheduleList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteSchedule(@NonNull ScheduleDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseScheduleList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseSchedule() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<ScheduleDto> scheduleList = GetScheduleList(new SerializableList<WirelessSocketDto>());
        for (int index = 0; index < scheduleList.getSize(); index++) {
            DeleteSchedule(scheduleList.getValue(index));
        }
    }

	/*
    Methods for ShoppingEntryDto
	 */

    public SerializableList<ShoppingEntryDto> GetShoppingList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseShoppingList.GetShoppingList();
    }

    public boolean SaveShoppingEntry(@NonNull ShoppingEntryDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseShoppingList.CreateEntry(newEntry);
        return true;
    }

    public void DeleteShoppingEntry(@NonNull ShoppingEntryDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        _databaseShoppingList.Delete(deleteEntry);
    }

    public void ClearDatabaseShoppingList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<ShoppingEntryDto> entries = GetShoppingList();
        for (int index = 0; index < entries.getSize(); index++) {
            DeleteShoppingEntry(entries.getValue(index));
        }
    }

	/*
    Methods for SocketDto
	 */

    public SerializableList<WirelessSocketDto> GetSocketList() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseSocketList.GetSocketList();
    }

    public boolean SaveSocket(@NonNull WirelessSocketDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseSocketList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteSocket(@NonNull WirelessSocketDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseSocketList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseSocket() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<WirelessSocketDto> socketList = GetSocketList();
        for (int index = 0; index < socketList.getSize(); index++) {
            DeleteSocket(socketList.getValue(index));
        }
    }

	/*
    Methods for TimerDto
	 */

    public SerializableList<TimerDto> GetTimerList(@NonNull SerializableList<WirelessSocketDto> socketList) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return new SerializableList<>();
        }

        return _databaseTimerList.GetTimerList(socketList);
    }

    public boolean SaveTimer(@NonNull TimerDto newEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseTimerList.CreateEntry(newEntry);
        return true;
    }

    public boolean DeleteTimer(@NonNull TimerDto deleteEntry) {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return false;
        }

        _databaseTimerList.Delete(deleteEntry);
        return true;
    }

    public void ClearDatabaseTimer() {
        if (!_isInitialized) {
            _logger.Error("Not initialized!");
            return;
        }

        SerializableList<TimerDto> timerList = GetTimerList(new SerializableList<WirelessSocketDto>());
        for (int index = 0; index < timerList.getSize(); index++) {
            DeleteTimer(timerList.getValue(index));
        }
    }
}
