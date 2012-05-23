/* 
SPAN - Smart Phone AdHoc Networking project
©2012 The MITRE Corporation
*/
package android.adhoc.manet.logger;

import android.adhoc.manet.R;
import android.adhoc.manet.logger.ManetLoggerService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ManetLoggerActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startService(new Intent(ManetLoggerActivity.this, ManetLoggerService.class));
    }
}