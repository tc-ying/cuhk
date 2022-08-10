package fdm.etl;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import weka.core.Instances;
import weka.gui.visualize.Plot2D;

/**
 * @description	Fixed sliding window plotter. Best to set time series as master data.
 * Left key goes back in time, right key goes forward.
 * Up and down keys to alter class label. W and S keys to alter y dimension. A and D keys to alter x dimension.
 *     
 * @date		
 * @author		Ying Ting Chung
 */
public class SlidePlot2D extends Plot2D implements KeyListener	{
	/** master data */
	private Instances masterData;
	/** sliding window data */
	private Instances slideData;
	/** date attribute index */
	private int date;
	/** x-coordinate of the plot */
	private int X;
	/** y-coordinate of the plot */
	private int Y;
	/** index of the attribute to use for colouring */
	private int cls;
	/** time point **/
	private int time;
	/** number of data points */
	private int windowSize;
	/** display meta-data */
	private JLabel label;
	
	/** Constructor initialising master data copy and label
	 * @param master - master data */
	public SlidePlot2D(Instances master)	{
		this.masterData = master;
		label = new JLabel();
		label.setForeground(Color.white);
		this.add(label, -1);
		for(int index = 0; index < master.numAttributes(); index++)	{
			if(master.attribute(index).isDate())	{
				this.date = index;
				break;}
		}
	}
	
	public SlidePlot2D(Instances master, int time, int windowSize)	{
		this.masterData = master;
		this.time = time;
		this.windowSize = windowSize;
		label = new JLabel();
		label.setForeground(Color.white);
		this.add(label, -1);
		for(int index = 0; index < master.numAttributes(); index++)	{
			if(master.attribute(index).isDate())	{
				this.date = index;
				break;}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 37)	{	// left key
			if(time > 0)	{
				time--;
				updatePlot();
			}
		} else if (e.getKeyCode() == 39)	{	// right key
			if(time + windowSize < masterData.numInstances())	{
				time++;
				updatePlot();
			}
		} else if (e.getKeyCode() == 38)	{	// up key
			if(cls > 0)	{
				cls--;
				updatePlot();
			}
		} else if (e.getKeyCode() == 40)	{	// down key
			if(cls < masterData.numAttributes() - 1)	{
				cls++;
				updatePlot();
			}
		} else if (e.getKeyCode() == 65)	{	// 'a' key
			if(X > 0)	{
				X--;
				updatePlot();
			}
		} else if (e.getKeyCode() == 68)	{	// 'd' key
			if(X < masterData.numAttributes() - 1)	{
				X++;
				updatePlot();
			}
		} else if (e.getKeyCode() == 87)	{	// 'w' key
			if(Y < masterData.numAttributes() - 1)	{
				Y++;
				updatePlot();
			}
		} else if (e.getKeyCode() == 83)	{	// 's' key
			if(Y > 0)	{
				Y--;
				updatePlot();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}
	
	/** Sets x-coordinate of the plot
	 * @param index - attribute index */
	public void setX(int index)	{this.X = index;}
	
	/** Sets y-coordinate of the plot
	 * @param index - attribute index */
	public void setY(int index)	{this.Y = index;}
	
	/** Sets index of the attribute to use for colouring
	 * @param index - attribute index */
	public void setC(int index)	{this.cls = index;}
	
	/** Sets beginning time point of sliding window
	 * @param time -  */
	public void setTime(int time)	{this.time = time;}
	
	/** Sets size of sliding window 
	 * @param windowSize - number of data points */
	public void setWindowSize(int windowSize)	{this.windowSize = windowSize;}
	
	public void updatePlot()	{
		WekaFilter f = new WekaFilter();
		StringBuilder sb = new StringBuilder();
		
		slideData = new Instances(masterData, time, windowSize);
		slideData.setClassIndex(cls);
		
	    try {
	    	if(!slideData.attribute(cls).isDate())	{
	    		slideData = f.fNumericToNominal(Integer.toString(cls+1), slideData);
	    	}
	    	setInstances(slideData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (slideData.classIndex() == -1)
	    	setCindex(slideData.numAttributes() - 1);
	    else
	    	setCindex(slideData.classIndex());
	
	    setXindex(X);
	    setYindex(Y);
	    sb.append("<html>");
	    sb.append(slideData.firstInstance().stringValue(date).substring(0, 10)).append(" to ").append(slideData.lastInstance().stringValue(date).substring(0, 10));
	    sb.append("<br>&nbsp; X: ").append(slideData.attribute(X).name()).append("&nbsp;-- Y: ").append(slideData.attribute(Y).name());
	    sb.append("<br>&nbsp; Label: ").append(slideData.attribute(slideData.classIndex()).name());
	    label.setText(sb.toString());
	    repaint();
	}
}
