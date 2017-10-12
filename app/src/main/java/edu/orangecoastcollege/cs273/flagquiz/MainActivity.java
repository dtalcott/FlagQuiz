package edu.orangecoastcollege.cs273.flagquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal.REGION;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final int FLAGS_IN_QUIZ = 10;

    private Button[] mButtons = new Button[8];
    private LinearLayout[] mLayouts = new LinearLayout[4];
    private List<Country> mAllCountriesList;  // all the countries loaded from JSON
    private List<Country> mQuizCountriesList; // countries in current quiz (just 10 of them)
    private List<Country> mFilteredCountriesList; //countries filtered by selected region
    private Country mCorrectCountry; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mFlagImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    private int mChoices; //Stores how many buttons selected
    private String mRegion; //Stores what region is selected

    //keys used for preferences.xml
    private static final String CHOICES = "pref_numberOfChoices";
    private static final String REGIONS = "pref_regions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Lets register the OnSharedPreferencesChangeListener
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);

        mQuizCountriesList = new ArrayList<>(FLAGS_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        //  Get references to GUI components (textviews and imageview)
        mFlagImageView = (ImageView) findViewById(R.id.flagImageView);
        mQuestionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);

        //  Put all 4 buttons in the array (mButtons)
        mButtons[0] = (Button) findViewById(R.id.button);
        mButtons[1] = (Button) findViewById(R.id.button2);
        mButtons[2] = (Button) findViewById(R.id.button3);
        mButtons[3] = (Button) findViewById(R.id.button4);
        mButtons[4] = (Button) findViewById(R.id.button5);
        mButtons[5] = (Button) findViewById(R.id.button6);
        mButtons[6] = (Button) findViewById(R.id.button7);
        mButtons[7] = (Button) findViewById(R.id.button8);

        //Put all Linear Layouts in an array (mLayouts)
        mLayouts[0] = (LinearLayout) findViewById(R.id.row1LinearLayout);
        mLayouts[1] = (LinearLayout) findViewById(R.id.row2LinearLayout);
        mLayouts[2] = (LinearLayout) findViewById(R.id.row3LinearLayout);
        mLayouts[3] = (LinearLayout) findViewById(R.id.row4LinearLayout);


        //  Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        //  Load all the countries from the JSON file using the JSONLoader
        try {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRegion = preferences.getString(REGIONS, "All");
        mChoices = Integer.parseInt(preferences.getString(CHOICES, "4"));
        updateChoices();
        updateRegions();
        //  Call the method resetQuiz() to start the quiz.
        resetQuiz();
    }

    /**
     * Sets up and starts a new quiz.
     */
    public void resetQuiz() {

        // Reset the number of correct guesses made
        mCorrectGuesses = 0;
        // Reset the total number of guesses the user made
        mTotalGuesses = 0;
        // Clear list of quiz countries (for prior games played)
        mQuizCountriesList.clear();
        // Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        // Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)
        int size = mFilteredCountriesList.size();
        while (mQuizCountriesList.size() < FLAGS_IN_QUIZ) {
            int randomPosition = rng.nextInt(size);
            Country randomCountry = mFilteredCountriesList.get(randomPosition);
            if (!mQuizCountriesList.contains(randomCountry))
                mQuizCountriesList.add(randomCountry);
        }

        //  Start the quiz by calling loadNextFlag
        loadNextFlag();
    }

    /**
     * Method initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 4 buttons, one of which contains the correct answer.
     */
    private void loadNextFlag() {
        //  Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        mCorrectCountry = mQuizCountriesList.remove(0);

        //  Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");

        //  Display current question number in the mQuestionNumberTextView
        int questionNumber = FLAGS_IN_QUIZ - mQuizCountriesList.size();
        mQuestionNumberTextView.setText(getString(R.string.question, questionNumber, FLAGS_IN_QUIZ));


        //  Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();
        String imageFileName = mCorrectCountry.getFileName();
        try {
            InputStream stream = am.open(imageFileName);
            Drawable image = Drawable.createFromStream(stream, mCorrectCountry.getName());
            mFlagImageView.setImageDrawable(image);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + mCorrectCountry.getFileName(), e);
        }

        // TODO: Get an InputStream to the asset representing the next flag
        // TODO: and try to use the InputStream to create a Drawable
        // TODO: The file name can be retrieved from the correct country's file name.
        // TODO: Set the image drawable to the correct flag.


        //  Shuffle the order of all the countries (use Collections.shuffle)
        Collections.shuffle(mFilteredCountriesList);
        do {
            Collections.shuffle(mFilteredCountriesList);
        } while (mFilteredCountriesList.subList(0, mChoices).contains(mCorrectCountry));


        //  Loop through all 4 buttons, enable them all and set them to the first 4 countries
        //  in the all countries list
        for (int i = 0; i < mChoices; i++) {
            mButtons[i].setEnabled(true);
            mButtons[i].setText(mFilteredCountriesList.get(i).getName());
        }

        // After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mChoices)].setText(mCorrectCountry.getName());
    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     *
     * @param v
     */
    public void makeGuess(View v) {
        // TODO: Downcast the View v into a Button (since it's one of the 4 buttons)
        // TODO: Get the country's name from the text of the button
        Button clickedButton = (Button) v;
        String guess = clickedButton.getText().toString();
        mTotalGuesses++;

        // TODO: If the guess matches the correct country's name, increment the number of correct guesses,
        if (guess.equals(mCorrectCountry.getName())) {
            //disable buttons if correct
            for (Button b : mButtons) {
                b.setEnabled(false);
            }
            mCorrectGuesses++;
            mAnswerTextView.setText(mCorrectCountry.getName());
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.correct_answer));

            if (mCorrectGuesses < FLAGS_IN_QUIZ) {
                //wait 2 seconds then load the next flag
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 2000);
            } else {
                //show an alert dialog then build it
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.results, mTotalGuesses, ((double) mCorrectGuesses / mTotalGuesses) * 100));
                builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                builder.setCancelable(false);
                builder.create();
                builder.show();
            }

        } else {
            clickedButton.setEnabled(false);
            mAnswerTextView.setText(getString(R.string.incorrect_answer));
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.incorrect_answer));
        }
        // TODO: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)
        // TODO: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
        // TODO: with the statistics and an option to Reset Quiz

        // TODO: Else, the answer is incorrect, so display "Incorrect Guess!" in red
        // TODO: and disable just the incorrect button.


    }


    @Override
    //Override the onCreateOptionsMenu to inflate the settings menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    //responds to user clicking the gear (settings)
    public boolean onOptionsItemSelected(MenuItem item) {
        //Make a new intent going to SettingsActivity
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            //figure out what key changed
            switch (key)
            {
                case CHOICES:
                    //read the number of choices from shared preferences
                    mChoices = Integer.parseInt(sharedPreferences.getString(CHOICES, "4"));
                    //call method to update choices (visually)
                    updateChoices();
                    resetQuiz();
                    break;

                case REGIONS:
                    mRegion = sharedPreferences.getString(REGION, "All");
                    updateRegions();
                    resetQuiz();
                    break;

            }

            //Notify user that the quiz will restart with new settings
            Toast.makeText(MainActivity.this, getString(R.string.restarting_quiz), Toast.LENGTH_SHORT).show();
        }
    };

    private void updateChoices()
    {
        //Enable/show all the linear layouts < mChoices/2
        //Disable/hide the others
        //Lets loop through all the linear layouts
        for (int i = 0; i < mLayouts.length; i++)
        {
            if (i < mChoices/2)
            {
                mLayouts[i].setEnabled(true);
                mLayouts[i].setVisibility(View.VISIBLE);
            }
            else
            {
                mLayouts[i].setEnabled(false);
                mLayouts[i].setVisibility(View.GONE);
            }
        }
    }

    private void updateRegions()
    {
        //Make a decision
        //If region is all; filtered list is all
        if (mRegion.equals("All"))
        {
            mFilteredCountriesList = new ArrayList<>(mAllCountriesList);
        }
        else
        {
            //Creates a new filtered list and deletes and previous countries in list
            mFilteredCountriesList = new ArrayList<>();
            Log.e(TAG, "Region = " + mRegion);
            //loop through all the countries
            for (Country c : mAllCountriesList)
            {
                if (c.getRegion().equals(mRegion))
                {
                    mFilteredCountriesList.add(c);
                }
            }
        }
    }

}
