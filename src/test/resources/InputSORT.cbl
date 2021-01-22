IDENTIFICATION DIVISION.
PROGRAM-ID.  InputSort.
      *> AUTHOR.  Michael Coughlan

ENVIRONMENT DIVISION.
INPUT-OUTPUT SECTION.
FILE-CONTROL.
    SELECT StudentFile ASSIGN TO "SORTSTUD.DAT"
		ORGANIZATION IS LINE SEQUENTIAL.
    SELECT WorkFile ASSIGN TO "WORK.TMP".


DATA DIVISION.
FILE SECTION.
FD StudentFile.
01 StudentDetails      PIC X(30).



SD WorkFile.
01 WorkRec.
   02 WStudentId       PIC 9(7).
   02 FILLER           PIC X(23).


PROCEDURE DIVISION.
Begin.
   SORT WorkFile ON ASCENDING KEY WStudentId
        INPUT PROCEDURE IS GetStudentDetails
        GIVING StudentFile.
   STOP RUN.


GetStudentDetails.
    DISPLAY "Enter student details using template below."
    DISPLAY "Enter no data to end.".
    DISPLAY "Enter - StudId, Surname, Initials, YOB, MOB, DOB, Course, Gender"
    DISPLAY "NNNNNNNSSSSSSSSIIYYYYMMDDCCCCG"
    ACCEPT  WorkRec.
    PERFORM UNTIL WorkRec = SPACES
       RELEASE WorkRec
       ACCEPT WorkRec
    END-PERFORM.
