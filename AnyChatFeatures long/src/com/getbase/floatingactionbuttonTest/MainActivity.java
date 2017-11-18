package com.getbase.floatingactionbuttonTest;

import android.app.Activity;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.anychatfeatures.R;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_frame);


        final View actionB = findViewById(R.id.action_b);
        FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
        actionC.setTitle("Hide/Show Action above");
        actionC.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        });
        ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).addButton(actionC);

        final FloatingActionButton removeAction = (FloatingActionButton) findViewById(R.id.button_remove);
        removeAction.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((FloatingActionsMenu) findViewById(R.id.multiple_actions_down)).removeButton(removeAction);
            }
        });

        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(getResources().getColor(R.color.white));
     

        final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
        actionA.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                actionA.setTitle("Action A clicked");
            }
        });

        // Test that FAMs containing FABs with visibility GONE do not cause
        // crashes
        findViewById(R.id.button_gone).setVisibility(View.GONE);
    }
}
