package com.tlcsdm.insightpc.service;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.util.List;

/**
 * Service class that wraps OSHI SystemInfo to provide system hardware
 * and operating system information.
 */
public class SystemInfoService {

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem operatingSystem;

    /**
     * Creates a service facade backed by a new OSHI SystemInfo instance.
     */
    public SystemInfoService() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.operatingSystem = systemInfo.getOperatingSystem();
    }

    /**
     * Get operating system information.
     *
     * @return OSHI operating system facade
     */
    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    /**
     * Get the hardware abstraction layer.
     *
     * @return OSHI hardware abstraction layer
     */
    public HardwareAbstractionLayer getHardware() {
        return hardware;
    }

    /**
     * Get the central processor.
     *
     * @return processor information and counters
     */
    public CentralProcessor getProcessor() {
        return hardware.getProcessor();
    }

    /**
     * Get global memory information.
     *
     * @return global physical and virtual memory information
     */
    public GlobalMemory getMemory() {
        return hardware.getMemory();
    }

    /**
     * Get the list of disk stores (physical disks).
     *
     * @return detected physical disks
     */
    public List<HWDiskStore> getDiskStores() {
        return hardware.getDiskStores();
    }

    /**
     * Get the list of network interfaces.
     *
     * @return detected network interfaces
     */
    public List<NetworkIF> getNetworkInterfaces() {
        return hardware.getNetworkIFs();
    }

    /**
     * Get the list of power sources (batteries).
     *
     * @return detected power sources
     */
    public List<PowerSource> getPowerSources() {
        return hardware.getPowerSources();
    }

    /**
     * Get the list of USB devices.
     *
     * @return USB device tree including connected devices
     */
    public List<UsbDevice> getUsbDevices() {
        return hardware.getUsbDevices(true);
    }

    /**
     * Get the computer system information.
     *
     * @return computer system, firmware, and baseboard information
     */
    public ComputerSystem getComputerSystem() {
        return hardware.getComputerSystem();
    }

    /**
     * Get the sensors (CPU temperature, fan speeds, voltage).
     *
     * @return sensor readings exposed by the operating system
     */
    public Sensors getSensors() {
        return hardware.getSensors();
    }

    /**
     * Get the list of displays.
     *
     * @return detected displays
     */
    public List<Display> getDisplays() {
        return hardware.getDisplays();
    }

    /**
     * Get the list of graphics cards.
     *
     * @return detected graphics cards
     */
    public List<GraphicsCard> getGraphicsCards() {
        return hardware.getGraphicsCards();
    }

    /**
     * Get the list of sound cards.
     *
     * @return detected sound cards
     */
    public List<SoundCard> getSoundCards() {
        return hardware.getSoundCards();
    }

    /**
     * Get OS family name.
     *
     * @return operating system family name
     */
    public String getOsFamily() {
        return operatingSystem.getFamily();
    }

    /**
     * Get OS version information as a string.
     *
     * @return formatted operating system version information
     */
    public String getOsVersionInfo() {
        return operatingSystem.getVersionInfo().toString();
    }

    /**
     * Get OS manufacturer.
     *
     * @return operating system manufacturer
     */
    public String getOsManufacturer() {
        return operatingSystem.getManufacturer();
    }

    /**
     * Get system uptime in seconds.
     *
     * @return uptime in seconds
     */
    public long getSystemUptime() {
        return operatingSystem.getSystemUptime();
    }

    /**
     * Get the number of running processes.
     *
     * @return current process count
     */
    public int getProcessCount() {
        return operatingSystem.getProcessCount();
    }

    /**
     * Get the number of running threads.
     *
     * @return current thread count
     */
    public int getThreadCount() {
        return operatingSystem.getThreadCount();
    }

    /**
     * Format bytes to a human-readable string.
     *
     * @param bytes the number of bytes
     * @return human-readable string (e.g., "8.0 GB")
     */
    private static final String[] BYTE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            return "N/A";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < BYTE_UNITS.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", value, BYTE_UNITS[unitIndex]);
    }

    /**
     * Format uptime seconds to a human-readable string.
     *
     * @param uptimeSeconds the uptime in seconds
     * @return formatted string like "2d 5h 30m 15s"
     */
    public static String formatUptime(long uptimeSeconds) {
        long days = uptimeSeconds / 86400;
        long hours = (uptimeSeconds % 86400) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }
}
