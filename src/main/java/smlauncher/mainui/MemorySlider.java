package smlauncher.mainui;

import com.sun.management.OperatingSystemMXBean;
import smlauncher.LaunchSettings;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.lang.management.ManagementFactory;

/**
 * A slider that changes the game's allotted memory.
 *
 * @author TheDerpGamer
 * @author SlavSquatSuperstar
 */
public class MemorySlider extends JSlider {

	private static final int MB_PER_GB = 1024;

	public MemorySlider() {
		super(SwingConstants.HORIZONTAL, MB_PER_GB * 2, getSystemMemory(), LaunchSettings.getMemory());
//		setBackground(Palette.paneColor);
		setDoubleBuffered(true);
		setOpaque(true);
		if (getSystemMemory() > MB_PER_GB * 16) { //Make sure the slider is not too squished for people that have a really epic gamer pc
			setMajorTickSpacing(MB_PER_GB * 2);
			setMinorTickSpacing(MB_PER_GB);
			setLabelTable(createStandardLabels(MB_PER_GB * 4));
		} else if (getSystemMemory() > MB_PER_GB * 8) {
			setMajorTickSpacing(MB_PER_GB);
			setMinorTickSpacing(MB_PER_GB / 2);
			setLabelTable(createStandardLabels(MB_PER_GB * 2));
		} else {
			setMajorTickSpacing(MB_PER_GB);
			setMinorTickSpacing(MB_PER_GB / 4);
			setLabelTable(createStandardLabels(MB_PER_GB));
		}
		setPaintTicks(true);
		setPaintLabels(true);
		setSnapToTicks(true);
	}

	public static int getSystemMemory() {
		try {
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			return (int) (os.getTotalPhysicalMemorySize() / (1024 * 1024));
		} catch (Exception exception) {
			System.err.println("Could not get system memory");
		}
		return 8192;
	}

}
