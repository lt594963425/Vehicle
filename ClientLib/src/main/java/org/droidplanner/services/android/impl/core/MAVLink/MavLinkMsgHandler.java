package org.droidplanner.services.android.impl.core.MAVLink;

import android.os.Message;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_altitude;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_power_status;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.enums.MAV_AUTOPILOT;
import com.MAVLink.enums.MAV_TYPE;

import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.utils.LogUtil;

/**
 * Parse the received mavlink messages, and update the drone state appropriately.
 */
public class MavLinkMsgHandler {

    public static final int AUTOPILOT_COMPONENT_ID = 1;

    private final MavLinkDroneManager droneMgr;

    public MavLinkMsgHandler(MavLinkDroneManager droneMgr) {
        this.droneMgr = droneMgr;
    }

    public void receiveData(MAVLinkMessage msg) {
        MAVLinkPacket receivedPacket;
        LogUtil.e("App", "------------MavLinkMsgHandler-------receiveData----------" + msg);
        if (msg.compid != AUTOPILOT_COMPONENT_ID) {
            return;
        }

        switch (msg.msgid) {
            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                LogUtil.e("App", "------------心跳包----------" + msg.msgid);
                msg_heartbeat msg_heart = (msg_heartbeat) msg;
                handleHeartbeat(msg_heart);
                break;
            default:
                break;
        }

        if (msg != null) {

            if (msg instanceof msg_sys_status) {
                //msg_power_status
                msg_sys_status power_status = (msg_sys_status) msg;
                LogUtil.e("App Data", "handleData: " + msg.getClass());

                String response = power_status.toString();
                LogUtil.e("App Data", "handleData: " + power_status.msgid);
                LogUtil.e("App Data", "handleData: " + power_status.toString());
            }
            if (msg instanceof msg_attitude) {
                //msg_power_status
                msg_attitude power_status = (msg_attitude) msg;
                String response = power_status.toString();
                LogUtil.e("App Data", "handleData: " + power_status.msgid);
                LogUtil.e("App Data", "handleData: " + power_status.toString());
            }
            if (msg instanceof msg_global_position_int) {
                //msg_power_status
                msg_global_position_int power_status = (msg_global_position_int) msg;
                String response = power_status.toString();
                LogUtil.e("App Data", "handleData: " + power_status.msgid);
                LogUtil.e("App Data", "handleData: " + power_status.toString());
            }

        } else {
            LogUtil.e("App Data", "handleData:----------------无数据 ");
        }


    }

    private void handleHeartbeat(msg_heartbeat heartbeat) {
        LogUtil.e("App", "------------MavLinkMsgHandler-------handleHeartbeat----------" + heartbeat.type);
        switch (heartbeat.autopilot) {
            case MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA:
                switch (heartbeat.type) {

                    case MAV_TYPE.MAV_TYPE_FIXED_WING:
                        droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_PLANE);
                        break;

                    case MAV_TYPE.MAV_TYPE_GENERIC:
                    case MAV_TYPE.MAV_TYPE_QUADROTOR:
                    case MAV_TYPE.MAV_TYPE_COAXIAL:
                    case MAV_TYPE.MAV_TYPE_HELICOPTER:
                    case MAV_TYPE.MAV_TYPE_HEXAROTOR:
                    case MAV_TYPE.MAV_TYPE_OCTOROTOR:
                    case MAV_TYPE.MAV_TYPE_TRICOPTER:
                        droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_COPTER);
                        break;

                    case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
                    case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                        droneMgr.onVehicleTypeReceived(FirmwareType.ARDU_ROVER);
                        break;
                }
                break;

            case MAV_AUTOPILOT.MAV_AUTOPILOT_PX4:
                droneMgr.onVehicleTypeReceived(FirmwareType.PX4_NATIVE);
                break;

            case MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC:
            case MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC_MISSION_FULL:
            case MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC_WAYPOINTS_ONLY:
            case MAV_AUTOPILOT.MAV_AUTOPILOT_GENERIC_WAYPOINTS_AND_SIMPLE_NAVIGATION_ONLY:
            default:
                droneMgr.onVehicleTypeReceived(FirmwareType.GENERIC);
                break;
        }

    }
}
