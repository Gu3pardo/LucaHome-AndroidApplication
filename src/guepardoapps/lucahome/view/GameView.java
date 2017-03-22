package guepardoapps.lucahome.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import guepardoapps.library.lucahome.common.constants.Color;
import guepardoapps.library.lucahome.common.constants.Constants;
import guepardoapps.library.lucahome.common.enums.MediaMirrorSelection;
import guepardoapps.library.lucahome.common.enums.ServerAction;
import guepardoapps.library.lucahome.common.tools.LucaHomeLogger;
import guepardoapps.library.lucahome.controller.MediaMirrorController;
import guepardoapps.library.lucahome.services.helper.NavigationService;

import guepardoapps.lucahome.R;

public class GameView extends Activity {

	private static final String TAG = GameView.class.getSimpleName();
	private LucaHomeLogger _logger;

	private String _selectedIp = MediaMirrorSelection.MEDIAMIRROR_1.GetIp();

	private boolean _pongIsRunning;
	private String _selectedPongPlayer = Constants.MEDIAMIRROR_PLAYER.get(0);

	private Context _context;
	private MediaMirrorController _mediaMirrorController;
	private NavigationService _navigationService;

	private Spinner _selectServerSpinner;

	private Button _buttonUp;
	private Button _buttonLeft;
	private Button _buttonRight;
	private Button _buttonDown;
	private Button _buttonRotate;

	private Spinner _selectPongPlayerSpinner;
	private Button _buttonPongPlay;
	private Button _buttonPongRestart;
	private Button _buttonPongStop;
	private Button _buttonPongPause;
	private Button _buttonPongResume;

	private Button _buttonSnakePlay;
	private Button _buttonSnakeStop;

	private Button _buttonTetrisPlay;
	private Button _buttonTetrisStop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_games);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.ACTION_BAR));

		_logger = new LucaHomeLogger(TAG);
		_logger.Debug("onCreate");

		_context = this;
		_mediaMirrorController = new MediaMirrorController(_context);
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
		super.onPause();
		_logger.Debug("onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_logger.Debug("onDestroy");
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

		_selectServerSpinner = (Spinner) findViewById(R.id.selectServerSpinner);
		final ArrayList<String> serverLocations = new ArrayList<String>();
		for (MediaMirrorSelection entry : MediaMirrorSelection.values()) {
			if (entry.GetId() > 0) {
				serverLocations.add(entry.GetLocation());
			}
		}
		ArrayAdapter<String> serverDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, serverLocations);
		serverDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_selectServerSpinner.setAdapter(serverDataAdapter);
		_selectServerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String selectedLocation = serverLocations.get(position);
				_logger.Debug(String.format("Selected location %s", selectedLocation));

				String selectedIp = MediaMirrorSelection.GetByLocation(selectedLocation).GetIp();
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

		_buttonLeft = (Button) findViewById(R.id.buttonLeft);
		_buttonLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String data = "";
				if (_pongIsRunning) {
					data = _selectedPongPlayer + ":" + Constants.DIR_LEFT;
				} else {
					data = Constants.DIR_LEFT;
				}
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_COMMAND.toString(), data);
			}
		});
		_buttonRight = (Button) findViewById(R.id.buttonRight);
		_buttonRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String data = "";
				if (_pongIsRunning) {
					data = _selectedPongPlayer + ":" + Constants.DIR_RIGHT;
				} else {
					data = Constants.DIR_RIGHT;
				}
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_COMMAND.toString(), data);
			}
		});
		_buttonUp = (Button) findViewById(R.id.buttonUp);
		_buttonUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_COMMAND.toString(),
						Constants.DIR_UP);
			}
		});
		_buttonDown = (Button) findViewById(R.id.buttonDown);
		_buttonDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_COMMAND.toString(),
						Constants.DIR_DOWN);
			}
		});
		_buttonRotate = (Button) findViewById(R.id.buttonRotate);
		_buttonRotate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_COMMAND.toString(),
						Constants.DIR_ROTATE);
			}
		});
	}

	private void initializeButtonPong() {
		_logger.Debug("initializeButtonPong");

		_selectPongPlayerSpinner = (Spinner) findViewById(R.id.selectPongPlayerSpinner);
		ArrayAdapter<String> serverDataAdapter = new ArrayAdapter<String>(_context,
				android.R.layout.simple_spinner_item, Constants.MEDIAMIRROR_PLAYER);
		serverDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_selectPongPlayerSpinner.setAdapter(serverDataAdapter);
		_selectPongPlayerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				_selectedPongPlayer = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		_buttonPongPlay = (Button) findViewById(R.id.buttonPongPlay);
		_buttonPongPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_pongIsRunning = true;
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_PONG_START.toString(), "");
			}
		});
		_buttonPongRestart = (Button) findViewById(R.id.buttonPongRestart);
		_buttonPongRestart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_PONG_RESTART.toString(), "");
			}
		});
		_buttonPongStop = (Button) findViewById(R.id.buttonPongStop);
		_buttonPongStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_pongIsRunning = false;
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_PONG_STOP.toString(), "");
			}
		});
		_buttonPongPause = (Button) findViewById(R.id.buttonPongPause);
		_buttonPongPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_pongIsRunning = false;
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_PONG_PAUSE.toString(), "");
			}
		});
		_buttonPongResume = (Button) findViewById(R.id.buttonPongResume);
		_buttonPongResume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_pongIsRunning = true;
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_PONG_RESUME.toString(), "");
			}
		});
	}

	private void initializeButtonSnake() {
		_logger.Debug("initializeButtonSnake");

		_buttonSnakePlay = (Button) findViewById(R.id.buttonSnakePlay);
		_buttonSnakePlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_SNAKE_START.toString(), "");
			}
		});
		_buttonSnakeStop = (Button) findViewById(R.id.buttonSnakeStop);
		_buttonSnakeStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_SNAKE_STOP.toString(), "");
			}
		});
	}

	private void initializeButtonTetris() {
		_logger.Debug("initializeButtonTetris");

		_buttonTetrisPlay = (Button) findViewById(R.id.buttonTetrisPlay);
		_buttonTetrisPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_TETRIS_START.toString(), "");
			}
		});
		_buttonTetrisStop = (Button) findViewById(R.id.buttonTetrisStop);
		_buttonTetrisStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				_mediaMirrorController.SendServerCommand(_selectedIp, ServerAction.GAME_TETRIS_STOP.toString(), "");
			}
		});
	}
}