package messaging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// This class is a singleton responsible for handling message distribution

public class Publisher {

	private static Publisher instance = null;
	private static ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<MessageListener>> subscribers;

	private Publisher() {

		// singleton. Use getInstance to access.
		subscribers = new ConcurrentHashMap<Class<?>, ConcurrentLinkedQueue<MessageListener>>();
	}

	public static Publisher getInstance() {
		if (instance == null) {
			instance = new Publisher();
		}
		return instance;
	}

	public synchronized void subscribe(Class<?> cls, MessageListener listener) {

		ConcurrentLinkedQueue<MessageListener> subscriberList = subscribers.get(cls);

		if (subscriberList == null) {
			subscriberList = new ConcurrentLinkedQueue<MessageListener>();
		}

		subscriberList.add(listener);
		subscribers.put(cls, subscriberList);
	}

	public static synchronized void unsubscribeAll() {
		subscribers.clear();
	}
	
	public void send(Message msg) {

		// Send message to all subscribers
		ConcurrentLinkedQueue<MessageListener> allListeners = subscribers.get(msg.getClass());
		if (allListeners != null) {
			for (MessageListener listener : allListeners) {
				listener.onMessage(msg);
			}
		}
	}
}
