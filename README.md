# DutWrapper
An unofficial wrapper at [sv.dut.udn.vn - Da Nang University of Science and Technology student page](http://sv.dut.udn.vn).

> [!NOTE]
> We will deprecate this project on 2025/09/11. Learn more on [project homepage](https://github.com/dutwrapper).

## Version
- Release version [![https://github.com/dutwrapper/dutwrapper-java](https://img.shields.io/github/v/release/dutwrapper/dutwrapper-java)](https://github.com/dutwrapper/dutwrapper-java/releases)
- Pre-release version [![https://github.com/dutwrapper/dutwrapper-java/tree/draft](https://img.shields.io/github/v/tag/dutwrapper/dutwrapper-java?label=pre-release%20tag)](https://github.com/dutwrapper/dutwrapper-java/tree/draft)
- [Summary change log](CHANGELOG.md) / [Entire source code changes](https://github.com/dutwrapper/dutwrapper-java/commits)
- Badge provided by [shields.io](https://shields.io/)

## Library requirements
- You will need JDK version 8 or above. Older version of JDK will result in compile error.
  - Currently I tested with Eclipse Temurin JDK with Hotspot v8 and v17.

## FAQ

### Branch in dutwrapper?
- `main`/`stable`: Default branch and main release.
- `draft`: This branch will used for update my progress and it is unstable. Use it at your own risk.

### I received error about login while running AccountTest?
- Make sure you have `dut_account` variable set with syntax `studentid|password`. This will ensure secure when testing project.

### Wiki for this library?
- Unfortunately, I haven't done wiki yet.
- Instead you can navigate source code in `src/test/java/io/dutwrapper/dutwrapper/` to view them.

### I'm got issue or a feature request about this library. How should I do?
- Navigate to [issue tab](https://github.com/dutwrapper/dutwrapper-java/issues) on this repository to create a issue or feature request.

## Credit and license?
- License: [**MIT**](LICENSE)
- DISCLAIMER:
  - This project - dutwrapper - is not affiliated with [Da Nang University of Science and Technology school](http://dut.udn.vn).
  - DUT, Da Nang University of Technology, web materials and web contents are trademarks and copyrights of [Da Nang University of Science and Technology school](http://dut.udn.vn).
- Used third-party dependencies:
  - [Jsoup](https://github.com/jhy/jsoup): Licensed under the [MIT License](https://github.com/jhy/jsoup/blob/master/LICENSE).
  - [OkHttp3](https://github.com/square/okhttp): Licensed under the [Apache License 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt).
  - [Gson](https://github.com/google/gson): Licensed under the [Apache License 2.0](https://github.com/google/gson/blob/master/LICENSE).
