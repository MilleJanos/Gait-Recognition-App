package ms.sapientia.ro.gaitrecognitionapp.view.menu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Calendar;

import ms.sapientia.ro.gaitrecognitionapp.R;
import ms.sapientia.ro.gaitrecognitionapp.common.AppUtil;
import ms.sapientia.ro.gaitrecognitionapp.common.AuthScoreRecyclerViewAdapter;
import ms.sapientia.ro.gaitrecognitionapp.common.FileUtil;
import ms.sapientia.ro.gaitrecognitionapp.logic.FirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.logic.MyFirebaseController;
import ms.sapientia.ro.gaitrecognitionapp.model.IAfter;
import ms.sapientia.ro.gaitrecognitionapp.model.ICallback;
import ms.sapientia.ro.gaitrecognitionapp.model.MyFirebaseUser;
import ms.sapientia.ro.gaitrecognitionapp.model.NavigationMenuFragmentItem;
import ms.sapientia.ro.gaitrecognitionapp.presenter.menu.ProfileFragmentPresenter;
import ms.sapientia.ro.gaitrecognitionapp.service.FirebaseUtils;
import ms.sapientia.ro.gaitrecognitionapp.view.MainActivity;

/**
 * This class is responsible to manage profile.
 *
 * @author MilleJanos
 */
public class ProfileFragment extends NavigationMenuFragmentItem implements ProfileFragmentPresenter.View {

    // Constant members:
    private static final String TAG = "ProfileFragment";
    private final int GALLERY_REQUEST_CODE = 98;
    // MVP members:
    private static ProfileFragmentPresenter mPresenter;
    // View members:
    private static TextView mTitleUserName;
    private static TextView mTitleUserEmail;
    private static LinearLayout mAuthScoreLinearLayout;
    private static LinearLayout mCollectedLinearLayout;
    private static TextView mAuthScore;
    private static TextView mCollectedScore;
    private static Button mRefreshButton;
    private static Button mClearButton;
    private static TextView mEmailTextView;
    private static TextView mFirstNameTextView;
    private static TextView mLastNameTextView;
    private static TextView mBirthDateTextView;
    private static TextView mPhoneNumberTextView;
    private FloatingActionButton mEditFloatingActionButton;
    private ImageView mHideImageView;
    private ConstraintLayout mAuthScoreWindowLinearLayout;
    private ImageView mProfileImageView;
    // Recycler View members:
    private static RecyclerView mRecyclerView;
    private static RecyclerView.Adapter mAdapter;
    private static RecyclerView.LayoutManager mLayoutManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.sInstance.setTitle("Profile");

        // Set presenter for MainActivity
        mPresenter = new ProfileFragmentPresenter(this);

        initView(view);
        bindClickListeners();

        initRecyclerView(view);

        refreshProfileInformationsUI();

        // Download and set profile:
        loadProfilePictureFromFirebase();
    }

    /**
     * This method bind the view elements.
     * @param view
     */
    private void initView(View view) {
        mTitleUserName = view.findViewById(R.id.user_name_textview);
        mTitleUserEmail = view.findViewById(R.id.user_email_textview);
        mAuthScoreLinearLayout = view.findViewById(R.id.auth_linearlayout);
        mCollectedLinearLayout = view.findViewById(R.id.data_collected_linearlayout);
        mAuthScore = view.findViewById(R.id.auth_value_textview);
        mCollectedScore = view.findViewById(R.id.data_collected_value_textview);
        mRefreshButton = view.findViewById(R.id.refresh_button);
        mClearButton = view.findViewById(R.id.clear_button);
        mEmailTextView = view.findViewById(R.id.email_textview);
        mFirstNameTextView = view.findViewById(R.id.first_name_textview);
        mLastNameTextView = view.findViewById(R.id.last_name_textview);
        mBirthDateTextView = view.findViewById(R.id.birth_date_textview);
        mPhoneNumberTextView = view.findViewById(R.id.phone_number_textview);
        mEditFloatingActionButton = view.findViewById(R.id.edit_floating_action_button);
        mHideImageView = view.findViewById(R.id.hide_image_view);
        mAuthScoreWindowLinearLayout = view.findViewById(R.id.score_container_frameLayout);
        mProfileImageView = view.findViewById(R.id.profile_picture_imageView);
    }

    /**
     * This method binds the listeners to view elements.
     */
    private void bindClickListeners() {
        mRefreshButton.setOnClickListener(v -> refreshProfileInformationsUI() );
        mClearButton.setOnClickListener(v -> resetAuthScore());
        mEditFloatingActionButton.setOnClickListener(v -> goToEditProfile());
        mAuthScoreLinearLayout.setOnClickListener( v -> {
            if ( mAuthScore.getText().equals("N/A") ){
                Toast.makeText(MainActivity.sContext,"Use authentication mode first!",Toast.LENGTH_LONG).show();
            }else{
                displayAuthScores();
            }
        });
        mHideImageView.setOnClickListener( v -> hideAuthScores() );
        mProfileImageView.setOnClickListener( v -> selectImage() );
    }

    /**
     * This method opens the edit profile page.
     */
    private void goToEditProfile() {
        MainActivity.sInstance.replaceFragment(new EditProfileFragment(),"edit_profile_fragment");
    }

    /**
     * This method refreshes the profile informations on UI.
     */
    public static void refreshProfileInformationsUI(){
        setTitleUserName( AppUtil.sUser.last_name, AppUtil.sUser.first_name);
        setTitleEmail( AppUtil.sAuth.getCurrentUser().getEmail());
        if( AppUtil.sUser.authenticaiton_values.size() > 0 ) {
            // Set last authenticated value:
            setAuthenticationScore(AppUtil.sUser.authenticaiton_values.get(AppUtil.sUser.authenticaiton_values.size() - 1), true);
        }else{
            setAuthenticationScore(-1,false);
        }
        setCollectedDataScore( AppUtil.sUser.raw_count );

        setEmail( AppUtil.sAuth.getCurrentUser().getEmail() );
        setFirstName( AppUtil.sUser.first_name );
        setLastName( AppUtil.sUser.last_name );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( AppUtil.sUser.birth_date );

        setBirthDate( calendar );
        setPhoneNumber( AppUtil.sUser.phone_number );

        refreshRecycler();
    }

    /**
     * This method resets the authentication values in the user object.
     */
    private void resetAuthScore(){
        AppUtil.sUser.authenticaiton_avg = 0;
        AppUtil.sUser.authenticaiton_values.clear();
        FirebaseController.setUserObject( AppUtil.sUser );
        refreshProfileInformationsUI();
        hideAuthScores();
        Toast.makeText(MainActivity.sContext,"Authentication scores cleared.",Toast.LENGTH_LONG).show();
    }

    // Top part:
    /**
     * This method sets the name and email on the page.
     * @param firstName first name to set.
     * @param lastName last name to set.
     */
    private static void setTitleUserName(String firstName, String lastName){
        if( ! firstName.isEmpty()){
            mTitleUserName.setText( firstName + " " + lastName );
        }else{
            mTitleUserName.setText( "" );
        }
    }

    /**
     * This method sets the user email.
     * @param email
     */
    private static void setTitleEmail(String email){
        if( ! email.isEmpty()){
            mTitleUserEmail.setText( email );
        }else{

        }
    }

    // Middle part:
    /**
     * This method sets the authentication score.
     * @param score
     * @param usePercentage
     */
    private static void setAuthenticationScore(double score, boolean usePercentage){
        if( score > 0 ){
            if( usePercentage ){
                int percentage = (int) Math.floor( score * 100 );
                mAuthScore.setText ( percentage + "%" );
            }else{
                mAuthScore.setText ( score + "");
            }
        }else{
            mAuthScore.setText ( "N/A" );
        }

    }

    /**
     * This method sets the collected data count value.
     * @param score
     */
    private static void setCollectedDataScore(double score){
        mCollectedScore.setText ( ((int) score) + "" );
    }

    // Bottom part:
    /**
     * This method sets the user's email.
     * @param email
     */
    private static void setEmail(String email) {
        if( ! email.isEmpty() ){
            mEmailTextView.setText( email );
        }else{
            mEmailTextView.setText("N/A");
        }
    }

    /**
     * This method sets the user's first name.
     * @param firstName
     */
    private static void setFirstName(String firstName) {
        if( ! firstName.isEmpty() ){
            mFirstNameTextView.setText( firstName );
        }else{
            mFirstNameTextView.setText("N/A");
        }
    }

    /**
     * This method sets the user's first name.
     * @param lastName
     */
    private static void setLastName(String lastName) {
        if( ! lastName.isEmpty() ){
            mLastNameTextView.setText( lastName );
        }else{
            mLastNameTextView.setText("N/A");
        }
    }

    /**
     * This method sets the user's birth date.
     * @param calendar
     */
    private static void setBirthDate(Calendar calendar) {
        if( calendar.getTimeInMillis() != 0 ){
            mBirthDateTextView.setText( formatDate( calendar ) );
        }else{
            mBirthDateTextView.setText( "N/A" );
        }
    }

    /**
     * This method sets the user's phone number.
     * @param phoneNumber
     */
    private static void setPhoneNumber(String phoneNumber) {
        if( ! phoneNumber.isEmpty() ){
            mPhoneNumberTextView.setText( phoneNumber );
        }else{
            mPhoneNumberTextView.setText("N/A");
        }
    }

    /**
     * This method refreshes the user object.
     * @param afterIt run Do() method after downloading the user object.
     */
    private static void refresh_sUser(IAfter afterIt){
        new FirebaseController().getUserObjectById(AppUtil.sUser.id, new ICallback<MyFirebaseUser>() {
            @Override
            public void Success(MyFirebaseUser user) {
                AppUtil.sUser = user;
                afterIt.Do();
            }

            @Override
            public void Failure() {
                Toast.makeText(MainActivity.sContext,"ERROR: 14", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failure: ERROR: 14: Can't download user object");
            }

            @Override
            public void Error(int error_code) {
                Toast.makeText(MainActivity.sContext,"ERROR: 15", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: ERROR: 15: Can't download user object");
            }
        });
    }

    /**
     * This method formats the date.
     * @param calendar
     */
    private static String formatDate(Calendar calendar){
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH) + 1;
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        return m +"/"+ d +"/"+ y;
    }

    /**
     * This method shows the authentication scores in recycler view.
     */
    private void displayAuthScores(){
        mAuthScoreWindowLinearLayout.setVisibility( View.VISIBLE );
        refreshRecycler();
    }

    /**
     * This method hides the authentication scores in recycler view.
     */
    private void hideAuthScores(){
        mAuthScoreWindowLinearLayout.setVisibility( View.INVISIBLE );
    }

    /**
     * This method shows the progress bar.
     */
    @Override
    public void showProgressBar() {
        MainActivity.sInstance.showProgressBar();
    }

    /**
     * This method hide the progress bar.
     */
    @Override
    public void hideProgressBar() {
        MainActivity.sInstance.hideProgressBar();
    }

    // Auth Score recycler view:\
    /**
     * This method initiates the Recycler View.
     * @param view view of the fragment.
     */
    private void initRecyclerView(android.view.View view){
        mRecyclerView = (RecyclerView) view.findViewById(R.id.auth_score_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(MainActivity.sContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new AuthScoreRecyclerViewAdapter( mPresenter.getDataSet() );
        mRecyclerView.setAdapter(mAdapter);

        // Code to Add an item with default animation
        //((TopicRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((TopicRecyclerViewAdapter) mAdapter).deleteItem(index);
    }

    /**
     * This method refreshes the recycler view data.
     */
    private static void refreshRecycler(){
        if( mAdapter != null && mRecyclerView != null ) {
            mAdapter = new AuthScoreRecyclerViewAdapter(mPresenter.getDataSet());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * This method lets the user to select image from gallery.
     */
    private void selectImage(){

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){

        if (resultCode == Activity.RESULT_OK)

            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    // Image selected:

                    Uri selectedImage = data.getData();

                    // Save into file:
                    String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "profile.jpg";
                    File file = new File(path);
                    FileUtil.createFileIfNotExists(file);

                    AppUtil.saveUriIntoFIle(selectedImage, path);

                    // Display it:
                    loadUrlInProfileImageView(selectedImage);

                    // Save to Firebase:
                    StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                            FirebaseUtils.STORAGE_DATA_KEY
                                    + "/" + AppUtil.sUser.id
                                    + "/" + "profile_"+ (AppUtil.sUser.profile_picture_idx + 1) +".jpg"
                    );

                    FirebaseController.uploadFile(ref, file, new ICallback() {
                        @Override
                        public void Success(Object user) {
                            // Update user object too:
                            AppUtil.sUser.profile_picture_idx++;
                            FirebaseController.setUserObject( AppUtil.sUser );
                        }

                        @Override
                        public void Failure() {
                            Log.e(TAG, "Failure: uploading profile: " + ref.toString() );
                        }

                        @Override
                        public void Error(int error_code) {
                            Log.e(TAG, "Error: uploading profile: " + ref.toString() );
                        }
                    });
                    break;
            }
    }

    /**
     * This method downloads profile picture from firebase.
     */
    public void loadProfilePictureFromFirebase(){
        if( AppUtil.sUser.profile_picture_idx != -1 ) {
            String path = AppUtil.internalFilesRoot.getAbsolutePath() + "/" + "profile.jpg";
            File file = new File(path);

            StorageReference ref = FirebaseUtils.firebaseStorage.getReference().child(
                    FirebaseUtils.STORAGE_DATA_KEY
                    + "/" + AppUtil.sUser.id
                    + "/" + "profile_"+ AppUtil.sUser.profile_picture_idx +".jpg"
            );

            new MyFirebaseController().downloadFile(ref, file, new ICallback<File>() {
                @Override
                public void Success(File user) {
                    if (file.exists()) {
                        loadImageFromLocalFile(file);
                    }
                }

                @Override
                public void Failure() {
                    Log.e(TAG, "Failure: downloading profile: " + ref.toString() );
                }

                @Override
                public void Error(int error_code) {
                    Log.e(TAG, "Error: downloading profile: " + ref.toString() );
                }
            });


        }
    }

    /**
     * This method loads the image from local file storage.
     * @param file into this file.
     */
    public void loadImageFromLocalFile(File file){
        if( file.exists() ) {
            loadUrlInProfileImageView( Uri.fromFile(file) );
        }else{
            Log.e(TAG,"File not exists! path:" + file.getAbsolutePath());
        }
    }

    /**
     * This method loads the image from uri.
     * @param uri to load.
     */
    public void loadUrlInProfileImageView(Uri uri){
        mProfileImageView.setImageURI(uri);
    }
}
