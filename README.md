# challenge ::: Loftsoft Project Challenge:
Following points covers in amount transfer project:
1.	Controller: I have created the transfer-amount rest controller. 
2.	We have added the request payload. Request payload name is MoneyTransferRequest.
3.	MoneyTransferRequest payload having the three attributes
	a.	accountFrom as String type
	b.	accountTo as String Type  
	c.	transferAmonut as BigDecimal
4.	Repositories: MoneyTransferRepository interface added and all add the three abstract class.
	a.	Debit(MoneyTransferRequest resuest); and returns type is boolean
	b.	Credit(MoneyTransferRequest resuest); and returns type is boolean
	c.	fundTransfer MoneyTransferRequest resuest); and returns type is MoneyTransferResponse
5.	MoneyTransferRepository is interface and there implementation class is AccountsRepositoryInMemory
6.	Money transfer business logic I have written inside fundTransfer method
7.	I have used the synchronized in debit, credit and block level in transfer method.
8.	I have also written the Junit test cases to cover all positive and negative cases.
9.	I have add all validation check in validation level.
10. I have covered all test cases.
11. Concorrent trsaction also thread safe that is also i have covered in junit test case



