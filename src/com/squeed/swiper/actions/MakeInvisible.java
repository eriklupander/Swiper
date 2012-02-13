package com.squeed.swiper.actions;

import android.view.View;

public class MakeInvisible implements Command {

	
	public void execute(Object... params) {
		((View)params[0]).setVisibility(View.INVISIBLE);
	}

}
