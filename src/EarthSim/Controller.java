package EarthSim;

import messaging.Message;
import messaging.Publisher;
import messaging.events.DisplayMessage;
import messaging.events.NeedDisplayDataMessage;
import messaging.events.ProduceContinuousMessage;
import messaging.events.ProduceMessage;
import simulation.Model;
import view.View;
import common.Buffer;
import common.ComponentBase;

public class Controller extends ComponentBase {
	
	public static final int DEFAULT_GRID_SPACING = 15;
	public static final int DEFAULT_TIME_STEP = 1;
	public static final float DEFAULT_PRESENTATION_RATE = 0.01f;
	
	private boolean debugMode = false;
	private Boolean simThreaded;
	private Boolean viewThreaded;
	
	private InitiativeSetting initiative;
	
	private Model model;
	private View view;
	
	private Publisher pub = Publisher.getInstance();
	
	private int bufferSize;
	private int debugCnt = 0;
	
	private Thread modelThread;
	private Thread viewThread;
	private Thread t;
	
	public Controller(Boolean simThreaded, Boolean viewThreaded, InitiativeSetting initiative, int bufferSize) {
		
		if (bufferSize < 1 || bufferSize > Integer.MAX_VALUE) 
			throw new IllegalArgumentException("Invalid size");
		
		this.simThreaded = simThreaded;
		this.viewThreaded = viewThreaded;
		this.initiative = initiative;
		this.bufferSize = bufferSize;
	}
	
	public void start() {
		start(DEFAULT_GRID_SPACING, DEFAULT_TIME_STEP, DEFAULT_PRESENTATION_RATE);
	}
	
	public void start(int gs, int timeStep, float presentationInterval) {
		
		if (gs < 1 || gs > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Invalid grid spacing");
		
		if (timeStep < 1 || gs > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Invalid time step");
		
		if (presentationInterval < 0)
			throw new IllegalArgumentException("Invalid presentation interval");
		
		Buffer.getBuffer().create(this.bufferSize);
		
		// Instance model/view
		model = new Model(gs, timeStep);
		view = new View(gs, timeStep, presentationInterval);
		
		// setup message subscriptions per initiative settings
		switch (initiative) {
		case MODEL:
			pub.subscribe(ProduceContinuousMessage.class, model);
			// kickstart message to the model.  After first message it will 
			// continue to provide the message to itself and fill buffer.
			pub.send(new ProduceContinuousMessage());
			break;

		case VIEW:
			pub.subscribe(NeedDisplayDataMessage.class, model);
			// the view will produce the above message any time the queue is 
			// empty.  When the model sees the event it will produce a single
			// simulation output for the view to display.
			break;

		case THIRD_PARTY:
			// This currently functions by telling the model to produce a single
			// sim result.  The controller then waits for the view to signal
			// it has displayed the data, before requesting the model produce
			// another sim result.
			pub.subscribe(ProduceMessage.class, model);
			// NOTE: no need to subscribe controller to display message since
			//       that is done in all cases for debug display counting.
			//       The process method for DisplayMessage will send all
			//       ProduceMessage's after the first below.
			pub.send(new ProduceMessage());
			break;
		}
		
		// subscribe to count presented results for debug purposes
		pub.subscribe(DisplayMessage.class, this);

		
		// Kick off threads as appropriate
		if(simThreaded) {
			modelThread = new Thread(model,"model");
			modelThread.start();
		}
		if(viewThreaded) {
			viewThread = new Thread(view,"view");
			viewThread.start();
		}
		
		// Kick off run loop
		paused = false;
		stopThread = false;
		if(t==null) {
			t = new Thread(this,"controller");
			t.start();
		}
	}
	
	public void stop() throws InterruptedException {
		// End run loop
		stopThread = true;
		paused = false;
		
		t.join();
		
		// Stop threads
		if(simThreaded) {
			modelThread.interrupt();
			modelThread.join();
		}
		if(viewThreaded) {
			viewThread.interrupt();
			viewThread.join();
		}
		
		// remove subscriptions
		Publisher.unsubscribeAll();
		
		// destroy model/view
		model.close();
		model = null;
		view.close();
		view = null;
		
		t = null;
	}
	
	public void pause() {
		
		// make GUI updates
		// set variable to skip run loop contents
		paused = true;
		model.pause(paused);
		view.pause(paused);
	}
	
	public void resume() {
		
		// make GUI updates
		// set variable to NOT skip run loop contents
		paused = false;
		model.pause(paused);
		view.pause(paused);
	}
	
	@Override
	public void runAutomaticActions() {
		if(debugMode && debugCnt >= 2) {
			stopThread = true;
		}
		
		// Allow non-threaded components to process event queues
		if(!simThreaded) {
			try {
				model.runAutomaticActions();
				model.processMessageQueue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!viewThreaded) {
			try {
				view.runAutomaticActions();
				view.processMessageQueue();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void dispatchMessage(Message msg) {
		
		if (msg instanceof DisplayMessage) {
			process((DisplayMessage) msg);
		} else {
			System.err.printf("WARNING: No processor specified in class %s for message %s\n",
					this.getClass().getName(), msg.getClass().getName());
		}
	}

	public void process(DisplayMessage msg) {
		debugCnt++;
		// If we're in third party mode and a display was just finished, it's
		// time to request another sim output.
		if(initiative == InitiativeSetting.THIRD_PARTY) {
			pub.send(new ProduceMessage());
		}
	}
}
