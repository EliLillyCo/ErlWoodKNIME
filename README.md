# Erl Wood KNIME

## Synopsis

The features include [KNIME](https://knime.com) nodes for working with authenticated file shares, other utility code and nodes geared towards pharmaceutical research with an emphasis on medicinal and computational chemistry. The nodes typically focus on SAR data manipulation and interpretation but also include generic tools such as a 2D/3D scatterplot viewer and extended Excel nodes.

For developers, we also include a framework for simplification of chemical structure conversion, node interface development, web service integration and enterprise authentication.

The code is structured as a collection of [KNIME](https://knime.com) extensions which consistute [Eclipse](https://www.eclipse.org) plugins and features implemented in [Java](https://java.com) built with [Maven](https://maven.apache.org) and [Tycho](https://www.eclipse.org/tycho/).

## Motivation

These nodes have been developed by the Research IT and Computational Drug Discovery groups at Erl Wood, United Kingdom. They are provided for free as part of Eli Lilly’s KNIME precompetitive strategy towards improving the efficiency of drug design worldwide.

## Installation

These features can be installed directly into your KNIME client.

* Select the _"Help"_ then _"Install New Software..."_ menu item.
* Select the KNIME Trusted Community Contributions update site from the drop-down menu.
* Expand the _"KNIME Community Extensions - Cheminformatics"_ element.
* Select the features prefixed with _"Erlwood"_.
* Click through the installation using the _"Next >"_ button at the bottom of the window.

[Erl Wood nodes on KNIME's Trusted Community site](https://www.knime.com/community/erlwood)

## Build

The project can be compiled and tested by executing the following Maven command line or utilising the Maven plugin in your IDE:

```bash
# Maven clean, run integration tests, compile and package the Eclipse features and plugins
mvn clean integration-test package
```

## Contributing

If you wish to contribute to this project, please fork from the master branch, make your changes and submit a pull request against the original project. We will endeavour to review these suggestions, and if they fit with the direction of the project and meet our coding standards we will accept them into the project.

## Repository

https://github.com/EliLillyCo/ErlWoodKNIME

## License

[GPL v3](LICENSE.txt)

[Creative Commons v3](CCLICENSE.txt) (for icons)
