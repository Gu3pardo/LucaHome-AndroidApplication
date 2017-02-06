package guepardoapps.lucahome.viewcontroller;

import android.content.Context;
import guepardoapps.lucahome.common.LucaHomeLogger;
import guepardoapps.lucahome.common.constants.Constants;
import guepardoapps.lucahome.common.controller.*;
import guepardoapps.lucahome.common.enums.LucaObject;
import guepardoapps.lucahome.common.enums.RaspberrySelection;
import guepardoapps.lucahome.dto.TimerDto;

public class TimerController {

	private static final String TAG = TimerController.class.getName();
	private LucaHomeLogger _logger;

	private Context _context;
	private ServiceController _serviceController;

	public TimerController(Context context) {
		_logger = new LucaHomeLogger(TAG);
		_context = context;
		_serviceController = new ServiceController(_context);
	}

	public void LoadTimer() {
		_logger.Debug("LoadTimer");
		_serviceController.StartRestService(Constants.SCHEDULE_DOWNLOAD, Constants.ACTION_GET_SCHEDULES,
				Constants.BROADCAST_DOWNLOAD_SCHEDULE_FINISHED, LucaObject.SCHEDULE, RaspberrySelection.BOTH);
	}

	public void Delete(TimerDto timer) {
		_logger.Debug("Delete: " + timer.toString());
		_serviceController.StartRestService(timer.GetName(), timer.GetCommandDelete(), Constants.BROADCAST_RELOAD_TIMER,
				LucaObject.TIMER, RaspberrySelection.BOTH);
	}
}
