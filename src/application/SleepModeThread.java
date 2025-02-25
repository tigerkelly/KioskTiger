package application;

import java.util.Date;

public class SleepModeThread extends Thread {
	
	private boolean stopLooping = false;
	private KioskTigerController ktc = null;
	private KtGlobal kg = KtGlobal.getInstance();

	public SleepModeThread(KioskTigerController ktc) {
		this.ktc = ktc;
	}
	
	@Override
	public void run() {
		
		while (stopLooping == false) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			long d = new Date().getTime() / 1000;
			
			if ((d - kg.lastAction) >= kg.sleepTime && kg.sleepMode == false) {
				kg.sleepMode = true;
//				System.out.println("Sleep now");
				ktc.sleepNow();
			}
		}
	}
	
	public void stopMoving() {
		stopLooping = true;
	}
}
