package de.imc.mirror.arapp.client.view;

import java.util.Comparator;

import de.imc.mirror.arapp.client.ARApp;

public abstract class View {

	protected ARApp instance;

	public static Comparator<String> COMPARESTRINGIGNORECASE = new Comparator<String>() {
		
		@Override
		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}
	};
	
	protected View(ARApp instance){
		this.instance = instance;
	}
	
	public native void log(String t) /*-{
		$wnd.console.log(t);
	}-*/;	
	
	
	protected abstract void build();
}
