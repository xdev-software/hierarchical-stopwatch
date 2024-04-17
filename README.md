[![Latest version](https://img.shields.io/maven-central/v/software.xdev/hierarchical-stopwatch?logo=apache%20maven)](https://mvnrepository.com/artifact/software.xdev/hierarchical-stopwatch)
[![Build](https://img.shields.io/github/actions/workflow/status/xdev-software/hierarchical-stopwatch/checkBuild.yml?branch=develop)](https://github.com/xdev-software/hierarchical-stopwatch/actions/workflows/checkBuild.yml?query=branch%3Adevelop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=xdev-software_hierarchical-stopwatch&metric=alert_status)](https://sonarcloud.io/dashboard?id=xdev-software_hierarchical-stopwatch)

# hierarchical-stopwatch

A hierarchical Java stopwatch that supports nesting and can be used to track performance across methods and classes. It also supports async.

Example output:
```
-------------------------------
Root    Parent  Task
-------------------------------
100,00% 100,00% - Run dummy [42ms]
   11,20%  11,20% - Launch tasks [5ms]
   88,74%  88,74% - Wait for tasks [38ms]
    ASYNC   ASYNC - Process 1 [11ms]
     12,45%  46,81% - Fetch [5ms]
     14,01%  52,70% - Process [6ms]
      0,13%   0,49% ? unspecified [0ms]
    ASYNC   ASYNC - Process 2 [32ms]
     12,44%  16,36% - Fetch [5ms]
     49,53%  65,14% - Process [21ms]
     13,95%  18,35% - Finalize [6ms]
      0,11%   0,14% ? unspecified [0ms]
    ASYNC   ASYNC - Process 3 [22ms]
     12,44%  23,71% - Fetch [5ms]
     39,99%  76,20% - Process [17ms]
      0,05%   0,10% ? unspecified [0ms]
    0,06%   0,06% ? unspecified [0ms]
```

An [usage example is available in the demo project](./hierarchical-stopwatch-demo/src/main/java/software/xdev/Application.java).

## Installation
[Installation guide for the latest release](https://github.com/xdev-software/hierarchical-stopwatch/releases/latest#Installation)

## Support
If you need support as soon as possible and you can't wait for any pull request, feel free to use [our support](https://xdev.software/en/services/support).

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

## Dependencies and Licenses
View the [license of the current project](LICENSE) or the [summary including all dependencies](https://xdev-software.github.io/hierarchical-stopwatch/dependencies)
