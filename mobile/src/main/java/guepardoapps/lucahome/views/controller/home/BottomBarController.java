package guepardoapps.lucahome.views.controller.home;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import es.dmoral.toasty.Toasty;
import guepardoapps.library.lucahome.common.dto.UserDto;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.LucaDialogController;
import guepardoapps.library.lucahome.services.helper.NavigationService;
import guepardoapps.library.lucahome.services.helper.UserService;

import guepardoapps.lucahome.R;
import guepardoapps.lucahome.views.ChangeView;
import guepardoapps.lucahome.views.InformationView;
import guepardoapps.lucahome.views.SettingsView;

public class BottomBarController {

    private static final String TAG = BottomBarController.class.getSimpleName();
    private LucaHomeLogger _logger;

    private Context _context;

    private LucaDialogController _dialogController;
    private NavigationService _navigationService;
    private UserService _userService;

    public BottomBarController(Context context) {
        _logger = new LucaHomeLogger(TAG);
        _context = context;

        _dialogController = new LucaDialogController(_context);
        _navigationService = new NavigationService(_context);
        _userService = new UserService(_context);
    }

    public void onCreate() {
        _logger.Debug("onCreate");

        initializeButton();
        initializeImageButton();
    }

    public void onResume() {
        _logger.Debug("onResume");
    }

    public void onPause() {
        _logger.Debug("onPause");
        _dialogController.Dispose();
    }

    public void onDestroy() {
        _logger.Debug("onDestroy");
        _dialogController.Dispose();
    }

    private void initializeButton() {
        _logger.Debug("initializeButton");

        String version;
        try {
            PackageInfo packageInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            _logger.Error(e.toString());
            version = "Error loading version...";
        }

        Button buttonVersionInformation = (Button) ((Activity) _context).findViewById(R.id.buttonVersionInformation);
        buttonVersionInformation.setText(version);
        buttonVersionInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(InformationView.class, true);
            }
        });
    }

    private void initializeImageButton() {
        _logger.Debug("initializeImageButton");

        ImageButton imageButtonChanges = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonChanges);
        imageButtonChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(ChangeView.class, true);
            }
        });

        ImageButton imageButtonUser = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonUser);
        imageButtonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserDto user = _userService.LoadUser();

                if (user == null) {
                    _logger.Error("Loaded user is null!");
                    Toasty.error(_context, "Loaded user is null!", Toast.LENGTH_LONG).show();
                    return;
                }

                _dialogController.ShowUserDetailsDialog(user);
            }
        });

        ImageButton imageButtonSettings = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonSettings);
        imageButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _navigationService.NavigateTo(SettingsView.class, true);
            }
        });

        ImageButton imageButtonAbout = (ImageButton) ((Activity) _context).findViewById(R.id.imageButtonAbout);
        imageButtonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .start(_context);
            }
        });
    }
}
