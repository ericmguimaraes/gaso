package com.ericmguimaraes.gaso.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ericmguimaraes.gaso.R;
import com.ericmguimaraes.gaso.adapters.ViewPagerAdapter;
import com.ericmguimaraes.gaso.config.Config;
import com.ericmguimaraes.gaso.fragments.GasFragment;
import com.ericmguimaraes.gaso.fragments.MapGasoFragment;
import com.ericmguimaraes.gaso.fragments.MyCarFragment;
import com.ericmguimaraes.gaso.fragments.StationFragment;
import com.ericmguimaraes.gaso.lists.MonthlyExpensesFragment;
import com.ericmguimaraes.gaso.persistence.CarDAO;
import com.ericmguimaraes.gaso.persistence.UserDAO;
import com.ericmguimaraes.gaso.util.ConnectionDetector;
import com.ericmguimaraes.gaso.util.GPSTracker;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GasFragment.OnFragmentInteractionListener, MyCarFragment.OnFragmentInteractionListener, MapGasoFragment.OnFragmentInteractionListener, StationFragment.OnListFragmentInteractionListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @BindString(R.string.gas)
    String gas;

    @BindString(R.string.spent)
    String spent;

    @BindString(R.string.my_car)
    String myCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(1);

        init();

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GasFragment(), gas);
        adapter.addFragment(new MyCarFragment(), myCar);
        adapter.addFragment(new MonthlyExpensesFragment(), spent);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    private void init(){
        UserDAO userDAO = new UserDAO(getApplicationContext());
        CarDAO carDAO = new CarDAO(getApplicationContext());
        if(Config.getInstance().currentUser==null)
            Config.getInstance().currentUser = userDAO.findFirst();
        if(Config.getInstance().currentCar==null)
            Config.getInstance().currentCar = carDAO.findFirst();
    }

    @Override
    public void onListFragmentInteraction(com.ericmguimaraes.gaso.fragments.dummy.DummyContent.DummyItem item) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
