/**
 * Copyright (C) 2011-2013 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.osc;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.neophob.sematrix.core.jmx.PacketAndBytesStatictics;
import com.neophob.sematrix.core.listener.MessageProcessor;
import com.neophob.sematrix.core.properties.ValidCommands;
import com.neophob.sematrix.osc.model.OscMessage;
import com.neophob.sematrix.osc.server.OscMessageHandler;
import com.neophob.sematrix.osc.server.OscServerException;
import com.neophob.sematrix.osc.server.impl.OscServer;
import com.neophob.sematrix.osc.server.impl.OscServerFactory;

/**
 * OSC Interface 
 * 
 * @author michu
 *
 */
public class PixelControllerOscServer extends OscMessageHandler implements PacketAndBytesStatictics {

	/** The log. */
	private static final Logger LOG = Logger.getLogger(PixelControllerOscServer.class.getName());

	//size of recieving buffer, should fit a whole image buffer
	private static final int BUFFER_SIZE = 50000;
	
	private OscServer oscServer;
	
	/**
	 * 
	 * @param listeningPort
	 * @throws OscServerException 
	 */
	public PixelControllerOscServer(int listeningPort) throws OscServerException {
	    if (listeningPort<1) {
	        LOG.log(Level.INFO, "Configured Port {0}, OSC Server disabled", new Object[] { listeningPort });
	        return;
	    }

		LOG.log(Level.INFO,	"Start OSC Server at port {0}", new Object[] { listeningPort });
		
		oscServer = OscServerFactory.createServer(this, listeningPort, BUFFER_SIZE);
	}

	/**
	 * 
	 */
	public void startServer() {
		oscServer.startServer();
	}
	
	//TODO stop server
	
	@Override
	public void handleOscMessage(OscMessage oscIn) {
		//sanity check
		if (StringUtils.isBlank(oscIn.getPattern())) {
			LOG.log(Level.INFO,	"Ignore empty OSC message...");
			return;
		}
		
		String pattern = oscIn.getPattern();
		
		ValidCommands command;		
		try {
			command = ValidCommands.valueOf(pattern);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Unknown message: "+pattern, e);
			return;			
		}
		
		String[] msg = new String[1+command.getNrOfParams()];
		msg[0] = pattern;
		
		if (oscIn.getBlob()==null && command.getNrOfParams()>0 &&
				command.getNrOfParams()!=oscIn.getArgs().length) {
			String args = oscIn.getArgs()==null ? "null" : ""+oscIn.getArgs().length; 
			LOG.log(Level.WARNING, "Parameter cound missmatch, expected: {0} available: {1} ",
					new String[]{""+command.getNrOfParams(), ""+args});
			return;
		}
		
		//ignore nr of parameter for osc generator
		if (command != ValidCommands.OSC_GENERATOR1 && command != ValidCommands.OSC_GENERATOR2) {			
			for (int i=0; i<command.getNrOfParams(); i++) {
				msg[1+i] = oscIn.getArgs()[i];
			}			
		}

		MessageProcessor.processMsg(msg, true, oscIn.getBlob());		
	}

	/**
	 * 
	 * @param theOscMessage
	 */
/*	public void oscEvent(OscMessage theOscMessage) {
		
		oscPacketCounter++;
		oscBytesRecieved += theOscMessage.getBytes().length;
		
		//address pattern -> internal message mapping
		String pattern = theOscMessage.addrPattern().trim().toUpperCase();
		try {
			//remove beginning "/"
			if (pattern.startsWith("/")) {
				pattern = pattern.substring(1, pattern.length());
			}
			ValidCommands command = ValidCommands.valueOf(pattern);
			String[] msg = new String[1+command.getNrOfParams()];
			msg[0] = pattern;
			byte[] blobData = null;

			for (int i=0; i<command.getNrOfParams(); i++) {

				//parse osc message
				if (theOscMessage.checkTypetag("s")) {
					msg[i+1] = theOscMessage.get(i).stringValue();
					LOG.log(Level.INFO,	"PARAM {0}", msg[i+1]); 
				} else
					if (theOscMessage.checkTypetag("i")) {
						msg[i+1] = ""+theOscMessage.get(i).intValue();
						LOG.log(Level.INFO,	"Received an osc message. with address pattern {0} typetag {1}.", 
								new Object[] { theOscMessage.addrPattern(), theOscMessage.typetag() });		
					} else
						if (theOscMessage.checkTypetag("f")) {
							msg[i+1] = ""+theOscMessage.get(i).floatValue();	
							LOG.log(Level.INFO,	"Received an osc message. with address pattern {0} typetag {1}.", 
									new Object[] { theOscMessage.addrPattern(), theOscMessage.typetag() });									
						} else {
							if (theOscMessage.checkTypetag("b")) {
								//binary blob
								blobData = theOscMessage.get(i).blobValue();	
							} else {
								LOG.log(Level.INFO, "Unknown typetag: "+theOscMessage.getTypetagAsBytes()[0]);
							}
						}
			}
			MessageProcessor.processMsg(msg, true, blobData);
			
	        //refresh gui
			Collector.getInstance().setTriggerGuiRefresh(true);			    
			
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Failed to parse OSC Message", e);
			return;
		}		
	}*/

/*	@Override
	public void oscStatus(OscStatus arg0) {
		// TODO Auto-generated method stub	
	}*/

    /* (non-Javadoc)
     * @see com.neophob.sematrix.core.jmx.PacketAndBytesStatictics#getPacketCounter()
     */
    @Override
    public int getPacketCounter() {
        return oscServer.getPacketCounter();
    }

    /* (non-Javadoc)
     * @see com.neophob.sematrix.core.jmx.PacketAndBytesStatictics#getBytesRecieved()
     */
    @Override
    public long getBytesRecieved() {
        return oscServer.getBytesRecieved();
    }	
	
	
}