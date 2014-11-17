# BAIDD #

BAIDD, or BDI Agents Interacting in Deliberation Dialogues, is a software tool to experiment with computational argumentation between several agents. It was originally build for [the AIDA project](http://ekok.nl/phd/), which was my PhD project at the Utrecht University. The science (and initial experimental results) can therefor be found in [my PhD thesis](http://ekok.nl/docs/thesis.pdf) ([summary](http://ekok.nl/docs/thesis-summary.txt)), which includes a chapter on the testbed software. 

### Goals ###

The software testbed was designed and has been used for finding proof that using argumentation can be beneficial for software agents. The focus has been on finding when and which benefits of argumentation apply, particularly in practical circumstances rather than theoretical possibility thereof.

The software is designed to be a testbed, such that it can be used to test many new strategies for computational argumentation as well as metric variants. This allows, for example, students interested in argumentation logics or (BDI-based) agent strategies to directly compare their strategy performance.

### Implementing and running dialogs ###

The core idea is that the software generate a deliberation scenario which in turn is played by agents through the application of a strategy. The resulting dialog is analyzed on several properties to provide useful metrics for efficiency and effectiveness. The language consists or several simple statements of which the playing of an argument is obviously the most important. The complexity arises from the embedded argumentation logic, which is an implementation variant of ASPIC.

Scenarios for agents to play can be generated dynamically (which is typically used when running experiments) or statically loaded from XML files. A single dialog, again, can be loaded from a static .baidd XML file, such as when using the graphical dialog viewer. Agent strategies are implemented directly in Java. Through the base classes (and default implementations) provided it is very easy to implement new BDI-variant strategies, but the agent logic is fully decoupled and an be fully written from scratch as well.

### Setup and running of experiments ###

The software is written in Java (Standard Edition). Three tools are provided:

* baidd-viewer is a graphical (Swing) interface to load agent strategies from a .baidd file and visually, step-by-step execute a dialog
* baidd-console is a command line tool to load agent strategies from a .baidd file and log the dialog and results to screen and file
* baidd-exp is a command line tool to execute repeated experiments and write results (metrics) to disc

Some example cases (preset scenario and BDI agents) can be found in BaiddTest, which can be directly loaded in the baidd-viewer to graphically execute a single dialog. For advanced analytics of experimental results with baidd-exp, some R (statistical analytics software) scripts are available, such as plotting of data per agent strategy.

### License ###

The BAIDD project code itself is released under the GNU GPL v3 license. Note that this is a copyleft license, such that any derivative work must be released under the same GNU GPL v3 open source license. The project makes uses (and includes the code) of the ASPIC Components library, which is released under the custom [ASPIC license](http://aspic.cossac.org/disclaimer.html) and further references itself to the SWI Prolog library. Further information of the ASPIC Components library is available on [the ASPIC website](http://aspic.cossac.org/components.html).

    Copyright 2010-2014 Eric Kok et al.
    
    BAIDD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    BAIDD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with BAIDD. If not, see <http://www.gnu.org/licenses/>.
    
### Contact ###

For lightweight help setting up the information or questions regarding its (code and functional) design you may contact me at [via email](mailto:erickok@gmail.com). See [my PhD project website](http://ekok.nl/phd/) for more information of the scientific background, resulting papers and further reading.