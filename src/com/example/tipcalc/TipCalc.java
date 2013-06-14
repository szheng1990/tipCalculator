package com.example.tipcalc;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class TipCalc extends Activity {
	
	private static final String TOTAL_BILL = "TOTAL_BILL";
	private static final String CURRENT_TIP = "CURRENT_TIP";
	private static final String BILL_WITHOUT_TIP = "BILL_WITHOUT_TIP";
	
	private double billBeforeTip;
	private double tipAmount;
	private double finalBill;
	
	private int[] checklistValues = new int[12];
	
	EditText billBeforeTipET;
	EditText tipAmountET;
	EditText finalBillET;
	
	SeekBar tipSeekBar;
	
	CheckBox friendlyCheckBox;
	CheckBox specialsCheckBox;
	CheckBox opinionCheckBox;
	
	RadioGroup availableRadioGroup;
	RadioButton badRadioButton;
	RadioButton okRadioButton;
	RadioButton goodRadioButton;
	
	Spinner problemSpinner;
	
	Button startButton;
	Button pauseButton;
	Button resetButton;
	
	Chronometer timeChronometer;
	
	long secondsYouWaited = 0;
	
	TextView timeWaitingTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calc);
        
        if(savedInstanceState == null){
        	/*just started the app*/
        	
        	billBeforeTip = 0.0;
        	tipAmount = 0.15;
        	finalBill = 0.0;
        }else{
        	billBeforeTip = savedInstanceState.getDouble(BILL_WITHOUT_TIP);
        	tipAmount = savedInstanceState.getDouble(CURRENT_TIP);
        	finalBill = savedInstanceState.getDouble(TOTAL_BILL);
        }
        
        /*initialize editText and seekBar*/
    	billBeforeTipET = (EditText) findViewById(R.id.billEditText);
    	tipAmountET = (EditText) findViewById(R.id.tipEditText);
    	finalBillET = (EditText) findViewById(R.id.finalBillEditText);
    	tipSeekBar = (SeekBar) findViewById(R.id.seekBar1);
    	
    	tipSeekBar.setOnSeekBarChangeListener(tipSeekBarListener);
    	
    	billBeforeTipET.addTextChangedListener(billBeforeTipListener);
    	
    	friendlyCheckBox = (CheckBox) findViewById(R.id.friendlyCheckBox);
    	specialsCheckBox = (CheckBox) findViewById(R.id.specialCheckBox);
    	opinionCheckBox = (CheckBox) findViewById(R.id.opinionCheckBox);
    	
    	setUpIntroCheckBoxes();
    	
    	availableRadioGroup = (RadioGroup) findViewById(R.id.availableRadioGroup);
    	badRadioButton = (RadioButton) findViewById(R.id.badRadio);
    	okRadioButton = (RadioButton) findViewById(R.id.okRadio);
    	goodRadioButton = (RadioButton) findViewById(R.id.goodRadio);
    	
    	addChangeListenerToRadios();
    	
    	problemSpinner = (Spinner) findViewById(R.id.problemsSpinner);
    	
    	addItemSelectedListenerToSpinner();
    	
    	startButton = (Button) findViewById(R.id.startButton);
    	pauseButton = (Button) findViewById(R.id.pauseButton);
    	resetButton = (Button) findViewById(R.id.resetButton);
    	
    	setButtonOnClickListeners();
    	
    	timeChronometer = (Chronometer) findViewById(R.id.timeChronometer);
    	
    	timeWaitingTextView = (TextView) findViewById(R.id.timeWaitingTextView);
    	
    }
    
    private void setButtonOnClickListeners() {
    	startButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				int stoppedMilliseconds = 0;
				
				String chronoText = timeChronometer.getText().toString();
				String array[] = chronoText.split(":");
				
				if (array.length==2){
					
					stoppedMilliseconds = Integer.parseInt(array[0])*60* 1000+
							Integer.parseInt(array[1])*1000;
				}else if (array.length==3){
					stoppedMilliseconds = Integer.parseInt(array[0])*3600* 1000+
							Integer.parseInt(array[1])*60*1000+
							Integer.parseInt(array[2])*1000;
				}else{//len == 1
					stoppedMilliseconds = Integer.parseInt(array[0])*1000;
				}
				
				timeChronometer.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
				
				secondsYouWaited = Long.parseLong(array[1]);
				
				updateTipBasedOnTimeWaited(secondsYouWaited);
				
				setTipFromWaitressChecklist();
				
				updateTipAndFinalBill();
				
				timeChronometer.start();
			}
    		
    	});
    	
    	pauseButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				timeChronometer.stop();
			}
    		
    	});
    	resetButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				timeChronometer.setBase(SystemClock.elapsedRealtime());
				timeChronometer.stop();
				
				secondsYouWaited = 0;
			}
    		
    	});
		
	}

	protected void updateTipBasedOnTimeWaited(long secondsYouWaited2) {
		checklistValues[9] = (secondsYouWaited2 > 10)?-2:2;
		
	}

	private void addItemSelectedListenerToSpinner() {
    	problemSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				checklistValues[6] = (problemSpinner.getSelectedItem().equals("Bad"))?-1:0;
				checklistValues[7] = (problemSpinner.getSelectedItem().equals("OK"))?3:0;
				checklistValues[8] = (problemSpinner.getSelectedItem().equals("Good"))?6:0;
				
				setTipFromWaitressChecklist();
				
				updateTipAndFinalBill();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
		
	}

	private void addChangeListenerToRadios() {
		availableRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				checklistValues[3] = (goodRadioButton.isChecked())?4:0;
				checklistValues[4] = (okRadioButton.isChecked())?2:0;
				checklistValues[5] = (badRadioButton.isChecked())?-1:0;
				
				setTipFromWaitressChecklist();
				
				updateTipAndFinalBill();
			}
			
		});
	}

	private void setUpIntroCheckBoxes() {
    	friendlyCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				checklistValues[0] = (friendlyCheckBox.isChecked())?4:0;
				
				setTipFromWaitressChecklist();
				
				updateTipAndFinalBill();
			}
    		
    	});
    	
      	specialsCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

    			@Override
    			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
    				checklistValues[1] = (specialsCheckBox.isChecked())?1:0;
    				
    				setTipFromWaitressChecklist();
    				
    				updateTipAndFinalBill();
    			}
        		
        	});
      	
      	opinionCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

    			@Override
    			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
    				checklistValues[2] = (opinionCheckBox.isChecked())?2:0;
    				
    				setTipFromWaitressChecklist();
    				
    				updateTipAndFinalBill();
    			}
        		
        	});
		
	}

	protected void setTipFromWaitressChecklist() {
		int checklistTotal = 0;
		
		for(int item: checklistValues){
			checklistTotal += item;
		}
		
		tipAmountET.setText(String.format("%.02f", checklistTotal * .01));
		
	}

	private TextWatcher billBeforeTipListener = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			
			try{
				billBeforeTip = Double.parseDouble(arg0.toString());
			}
			
			catch(NumberFormatException e){
				billBeforeTip = 0.0;
			}
			
			updateTipAndFinalBill();
			
		}
    	
    };
    
    private OnSeekBarChangeListener tipSeekBarListener = new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			tipAmount = (tipSeekBar.getProgress()) * 0.01;
			tipAmountET.setText(String.format("%.02f", tipAmount));
			
			updateTipAndFinalBill();
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
    private void updateTipAndFinalBill(){
    	double tipAmount = Double.parseDouble(tipAmountET.getText().toString());
    	double finalBill = billBeforeTip * (1 + tipAmount);
    	
    	finalBillET.setText(String.format("%.02f", finalBill));
    }
    
    protected void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	
    	outState.putDouble(TOTAL_BILL, finalBill);
    	outState.putDouble(BILL_WITHOUT_TIP, billBeforeTip);
    	outState.putDouble(CURRENT_TIP, tipAmount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tip_calc, menu);
        return true;
    }
}
