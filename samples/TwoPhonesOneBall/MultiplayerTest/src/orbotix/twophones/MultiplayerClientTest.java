package orbotix.twophones;

import android.test.AndroidTestCase;
import orbotix.robot.base.*;
import orbotix.robot.multiplayer.LocalMultiplayerClient;
import orbotix.robot.multiplayer.RemotePlayer;

/**
 * Created by Orbotix Inc.
 * Date: 2/6/12
 *
 * @author Adam Williams
 */
public class MultiplayerClientTest extends AndroidTestCase {



    public void testSendDeviceCommandTo(){

        LocalMultiplayerClient client = new LocalMultiplayerClient(getContext());
        RemotePlayer p = new RemotePlayer();

        //Boost command
        DeviceCommand command = new BoostCommand(10f, 180f);
        client.sendDeviceCommandTo(command, p);

        //Calibrate command
        command = new CalibrateCommand(0f);
        client.sendDeviceCommandTo(command, p);

        //FrontLEDOutputCommand
        command = new FrontLEDOutputCommand(1f);
        client.sendDeviceCommandTo(command, p);

        //RollCommand
        command = new RollCommand(180f, 1f, false);
        client.sendDeviceCommandTo(command, p);

        //RawMotorCommand
        command = new RawMotorCommand(1, 1, 1, 0);
        client.sendDeviceCommandTo(command, p);

        //RGBLEDOutputCommand
        command = new RGBLEDOutputCommand(255, 255, 255);
        client.sendDeviceCommandTo(command, p);

        //RotationRateCommand
        command = new RotationRateCommand(1f);
        client.sendDeviceCommandTo(command, p);

        //SleepCommand
        command = new SleepCommand(120, 0xff);
        client.sendDeviceCommandTo(command, p);

        //StabilizationCommand
        command = new StabilizationCommand(true);
        client.sendDeviceCommandTo(command, p);
    }
}
