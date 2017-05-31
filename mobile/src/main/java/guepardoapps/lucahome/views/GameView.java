package guepardoapps.lucahome.views;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.enums.MediaServerSelection;
import guepardoapps.library.lucahome.common.enums.MediaServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.lucahome.R;

public class GameView extends Activity {

    private static final String TAG = GameView.class.getSimpleName();
    private LucaHomeLogger _logger;

    private String _selectedIp = MediaServerSelection.MEDIAMIRROR_1.GetIp();

    private boolean _pongIsRunning;
    private String _selectedPongPlayer = Constants.MEDIAMIRROR_PLAYER.get(0);

    private Context _context;
    private MediaMirrorController _mediaMirrorController;
    private NavigationService _navigationService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_games);

        _logger = new LucaHomeLogger(TAG);
        _logger.Debug("onCreate");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));
        } else {
            _logger.Error("ActionBar is null!");
        }

        _context = this;
        _mediaMirrorController = new MediaMirrorController(_context);
        _mediaMirrorController.Initialize();
        _navigationService = new NavigationService(_context);

        initializeSpinner();
        initializeButtonControl();
        initializeButtonPong();
        initializeButtonSnake();
        initializeButtonTetris();
    }

    @Override
    public void onResume() {
        super.onResume();
        _logger.Debug("onResume");
    }

    @Override
    public void onPause() {
        _logger.Debug("onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _logger.Debug("onDestroy");
        _mediaMirrorController.Dispose();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            _navigationService.NavigateTo(HomeView.class, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initializeSpinner() {
        _logger.Debug("initializeSpinner");

        Spinner selectServerSpinner = (Spinner) findViewById(R.id.selectServerSpinner);
        final ArrayList<String> serverLocations = new ArrayList<>();
        for (MediaServerSelection entry : MediaServerSelection.values()) {
            if (entry.GetId() > 0) {
                serverLocations.add(entry.GetLocation());
            }
        }
        ArrayAdapter<String> serverDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, serverLocations);
        serverDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectServerSpinner.setAdapter(serverDataAdapter);
        selectServerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = serverLocations.get(position);
                _logger.Debug(String.format("Selected location %s", selectedLocation));

                String selectedIp = MediaServerSelection.GetByLocation(selectedLocation).GetIp();
                if (selectedIp != null) {
                    _selectedIp = selectedIp;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initializeButtonControl() {
        _logger.Debug("initializeButtonControl");

        Button buttonLeft = (Button) findViewById(R.id.buttonLeft);
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data;
                if (_pongIsRunning) {
                    data = _selectedPongPlayer + ":" + Constants.DIR_LEFT;
                } else {
                    data = Constants.DIR_LEFT;
                }
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_COMMAND.toString(), data);
            }
        });

        Button buttonRight = (Button) findViewById(R.id.buttonRight);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data;
                if (_pongIsRunning) {
                    data = _selectedPongPlayer + ":" + Constants.DIR_RIGHT;
                } else {
                    data = Constants.DIR_RIGHT;
                }
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_COMMAND.toString(), data);
            }
        });

        Button buttonUp = (Button) findViewById(R.id.buttonUp);
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_COMMAND.toString(), Constants.DIR_UP);
            }
        });

        Button buttonDown = (Button) findViewById(R.id.buttonDown);
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_COMMAND.toString(), Constants.DIR_DOWN);
            }
        });

        Button buttonRotate = (Button) findViewById(R.id.buttonRotate);
        buttonRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_COMMAND.toString(), Constants.DIR_ROTATE);
            }
        });
    }

    private void initializeButtonPong() {
        _logger.Debug("initializeButtonPong");

        Spinner selectPongPlayerSpinner = (Spinner) findViewById(R.id.selectPongPlayerSpinner);
        ArrayAdapter<String> serverDataAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_spinner_item, Constants.MEDIAMIRROR_PLAYER);
        serverDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectPongPlayerSpinner.setAdapter(serverDataAdapter);
        selectPongPlayerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _selectedPongPlayer = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button buttonPongPlay = (Button) findViewById(R.id.buttonPongPlay);
        buttonPongPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _pongIsRunning = true;
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_PONG_START.toString(), "");
            }
        });

        Button buttonPongRestart = (Button) findViewById(R.id.buttonPongRestart);
        buttonPongRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_PONG_RESTART.toString(), "");
            }
        });

        Button buttonPongStop = (Button) findViewById(R.id.buttonPongStop);
        buttonPongStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _pongIsRunning = false;
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_PONG_STOP.toString(), "");
            }
        });

        Button buttonPongPause = (Button) findViewById(R.id.buttonPongPause);
        buttonPongPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _pongIsRunning = false;
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_PONG_PAUSE.toString(), "");
            }
        });

        Button buttonPongResume = (Button) findViewById(R.id.buttonPongResume);
        buttonPongResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _pongIsRunning = true;
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_PONG_RESUME.toString(), "");
            }
        });
    }

    private void initializeButtonSnake() {
        _logger.Debug("initializeButtonSnake");

        Button buttonSnakePlay = (Button) findViewById(R.id.buttonSnakePlay);
        buttonSnakePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_SNAKE_START.toString(), "");
            }
        });

        Button buttonSnakeStop = (Button) findViewById(R.id.buttonSnakeStop);
        buttonSnakeStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_SNAKE_STOP.toString(), "");
            }
        });
    }

    private void initializeButtonTetris() {
        _logger.Debug("initializeButtonTetris");

        Button buttonTetrisPlay = (Button) findViewById(R.id.buttonTetrisPlay);
        buttonTetrisPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_TETRIS_START.toString(), "");
            }
        });

        Button buttonTetrisStop = (Button) findViewById(R.id.buttonTetrisStop);
        buttonTetrisStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mediaMirrorController.SendCommand(_selectedIp, MediaServerAction.GAME_TETRIS_STOP.toString(), "");
            }
        });
    }
}