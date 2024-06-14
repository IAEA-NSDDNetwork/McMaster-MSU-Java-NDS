# McMaster-MSU Java-NDS 
NDS publishing code. From an ENSDF file, produces a Nuclear Data Sheet pdf. 

McMaster-MSU Java-NDS is part of the [ENSDF Analysis and Utility Programs](https://nds.iaea.org/public/ensdf_pgm/).

Please address any feedback to Jun Chen chenj@frib.msu.edu

## Change history

#### 2024-06
CRITICAL UPDATE:
update NSR retrieval methods (for retrieving reference infomation when generating the reference list) with the new NSR search engine (online Jun 2024). 

Note that all previous versions using the old NSR search engine are no longer working for retrieving reference information due to offline of the old NSR.

#### 2024-05-21
Minor improvements

#### 2024-03-12
Bug fixes

#### 2024-02-20
Bug fixes and improvements

#### 2023-08-29
Bug fixes and improvements

#### 2023-05-25
Bug fixes and improvements

#### 2023-04-14
Revised to display dataset title as "EC+B+" if Q(EC)>1020 keV or 
"EC" if Q(EC)<=1020 for decay mode="EC" in DSID based on Q(EC), the
same way the label of decay mode has been displayed.

#### 2023-03-20
Fix a bug that the last comment in a table page is duplicated at the beginning of next page. 

#### 2023-03-09
Fix rounding issue of BR without uncertainty in normalization record, which rounds up BR to keep 3 digits after it is multiplied by 100.
All digits should be kept as they are in the BR record.

#### 2023-03-01
Add an option for auto-correction of keynumbers with incorrect letter-case for PDF output.
Right click on "Create LaTeX File" button to get this option.

#### 2023-02-09
Bug fix

#### 2022-09-28
Bug fix

#### 2022-08-18
Few bug fixes, e.g., in-table comment lines at page bottom being printed again at the top of next page

#### 2022-06-30
Regular update with some minor bug fixes.

#### 2022-04-02
Regular update with some minor bug fixes and improvements.

#### 2021-12-19
Fixed a bug that causes limits in continuation records not being printed correctly.

#### 2021-12-09
Few bug fixes, e.g., missing flagged footnotes in delayed particle tables.

#### 2021-03
Bug fix: in datasets where there are bands based on different unknown levels X and Y, gamma rays from some member states in those bands could be mistakenly identified as unplaced transitions by the previous version

#### 2021-02 
Bug fixing

#### 2019-09
A few errors and bugs (mostly minor) have been reported and fixed, for example, incorrect arrows for decay modes in the skeleton drawing.

#### 2019-04
Version 1.9.22

#### 2019-01 
Version 1.9_19 December 2018

#### 2018-11 
Version 1.8.1. Fix for reference list generation

#### 2018-07
Version 1.8

#### 2018-01
Version 1.7

#### 2017-07
Version 1.6

#### 2017-06 
Version 1.5_01

#### 2017-03 
ENSDF publishing code
Java-nds produces Nuclear Data Sheets-like pdf file from a ENSDF input

## Disclaimer

Neither the author nor anybody else makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy, completeness or usefulness of any information disclosed, or represents that its use would not infringe privately owned rights.
