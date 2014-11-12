package view;

import common.Buffer;
import common.ComponentBase;
import common.IBuffer;
import common.IGrid;
import messaging.Message;
import messaging.Publisher;
import messaging.events.DisplayMessage;
import messaging.events.NeedDisplayDataMessage;

public class View extends ComponentBase {

	// 1e-12f;
	private final float STABLE_THRESHOLD = 0f;
	
	// set true to instrument stats (NOTE: some of these will change execution timing)
	private final boolean STATISTIC_MODE = false; 
	
	private Publisher pub = Publisher.getInstance();
	
	EarthDisplay display = null;

	// flag used to keep us from requesting more than once before getting response
	boolean displayRequestPending = false; 

	// set to true when initial conditions are overcome
	boolean steadyState = false; 
	
	// Profiling fields
	float statInterval = 1.0f;
		
	// Steady state assumed when when average equator temperature stabilizes
	float lastEquatorAverage = 0.0f;
	float presentationInterval;
	int timeStep;
	
	// used to throttle presentation rate
	long lastDisplayTime = 0;
	long lastStatTime = 0;
	long maxUsedMem = 0;
	long startWallTime;
	long startCpuTime;
	long presentationCnt = 1;

	public View(int gs, int timeStep, float presentationInterval) {
		
		this.timeStep = timeStep;
		this.presentationInterval = presentationInterval;
		this.display = new EarthDisplay();
		display.display(gs, timeStep);
		display.update((IGrid) null);
	}

	@Override
	public void dispatchMessage(Message msg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void runAutomaticActions() throws InterruptedException {
		// Don't do anything if enough time hasn't passed for us to display
		// another datapoint

		long curTime = System.nanoTime();
		if ((curTime - lastDisplayTime) * 1e-9 < presentationInterval) {
			return;
		}

		// Check to see if there is anything in the data queue to process
		IGrid data = null;
		data = Buffer.getBuffer().get();

		if (data != null) {
			if (STATISTIC_MODE) {

				if (!steadyState && steadyStateReached(data)) {
					steadyState = true;
					System.out.printf("========STABLE REACHED!========: %d %d\n",
							data.getCurrentTime(), data.getCurrentTime()/timeStep);
				}

				// Sample memory usage periodically
				if ((curTime - lastStatTime) * 1e-9 > statInterval) {
					float wallTimePerPresentation = (float) (System.nanoTime() - startWallTime)
							/ presentationCnt;
					System.out.printf("walltime/present (msec): %f\n",
							wallTimePerPresentation / 1e6);
					Runtime runtime = Runtime.getRuntime();
					System.gc();
					maxUsedMem = Math.max(maxUsedMem, runtime.totalMemory()
							- runtime.freeMemory());
					System.out.printf("usedMem: %.1f\n", maxUsedMem / 1e6);
					lastStatTime = curTime;

					IBuffer b = Buffer.getBuffer();
					System.out.printf("Buffer fill status: %d/%d\n",
							b.size() + 1, b.size() + b.getRemainingCapacity());

					startWallTime = System.nanoTime();
					presentationCnt = 0;

				}
				presentationCnt++;
			}
			present(data);
			lastDisplayTime = curTime;
			displayRequestPending = false;
		} else {
			if (!displayRequestPending) {
				pub.send(new NeedDisplayDataMessage());
				displayRequestPending = true;
			}
		}
	}

	private void present(IGrid data) {

		display.update(data);
		pub.send(new DisplayMessage());
	}

	public void close() {
		// destructor when done with class
		display.close();
	}

	public boolean steadyStateReached(IGrid grid) {
		float equatorAverage = 0.0f;
		int eqIdx = grid.getGridHeight() / 2;
		for (int i = 0; i < grid.getGridWidth(); i++) {
			equatorAverage += grid.getTemperature(i, eqIdx);
		}
		equatorAverage /= grid.getGridWidth();

		boolean stable = false;

		if (Math.abs(equatorAverage - lastEquatorAverage) <= STABLE_THRESHOLD) {
			stable = true;
		}
		lastEquatorAverage = equatorAverage;
		return stable;

	}
}
