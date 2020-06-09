program test;
var a: integer; 
type
	id = record
		firstname, surname : STRING;
		phone: integer;
		paidCurrentSubscription: boolean;
	end;

begin
   a := 10;
   repeat
      write('value of a: ', a);
      a := a + 1;
   until a = 20;
end.


