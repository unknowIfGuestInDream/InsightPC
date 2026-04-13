# InsightPC

A cross-platform system information visualizer built with JavaFX and OSHI.

## Features

- **System Overview**: Operating system, manufacturer, model, and uptime
- **CPU Information**: Processor details, core counts, frequency, and real-time usage monitoring
- **Memory Monitoring**: Physical memory, virtual memory, swap space, and memory stick details
- **Disk Information**: Disk drives, file systems, and storage usage
- **Network Interfaces**: Network adapters, MAC addresses, IP addresses, and traffic statistics
- **Process Management**: Running processes with PID, memory usage, and CPU consumption
- **Multi-language Support**: English, Chinese (Simplified), and Japanese
- **Theme Support**: AtlantaFX themes (Primer Light/Dark, Nord Light/Dark)
- **User Preferences**: Persistent settings for language and theme

## Download

- **Release**: <https://github.com/unknowIfGuestInDream/InsightPC/releases>
- **Jenkins**: <https://jenkins.tlcsdm.com/job/Tool/job/InsightPC>

## Screenshots

The screenshots below are from the application UI and are stored in `/readme`.

<p align="center">
  <img src="readme/overview.png" alt="Overview" width="45%" />
  <img src="readme/cpu.png" alt="CPU" width="45%" />
</p>
<p align="center">
  <img src="readme/memory.png" alt="Memory" width="45%" />
  <img src="readme/storage.png" alt="Storage" width="45%" />
</p>
<p align="center">
  <img src="readme/network.png" alt="Network" width="45%" />
  <img src="readme/processes.png" alt="Processes" width="45%" />
</p>
<p align="center">
  <img src="readme/variables.png" alt="Variables" width="45%" />
  <img src="readme/power.png" alt="Power" width="45%" />
</p>
<p align="center">
  <img src="readme/usb.png" alt="USB" width="45%" />
  <img src="readme/detail.png" alt="Detail" width="45%" />
</p>

## Requirements

- Java 21 or later
- Maven 3.9+

## Getting Started

### Build

```bash
mvn clean package
```

### Run

```bash
mvn javafx:run
```

### Test

```bash
mvn clean verify
```

## License

[MIT License](LICENSE)
