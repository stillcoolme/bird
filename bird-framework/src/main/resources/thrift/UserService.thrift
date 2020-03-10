namespace java com.stillcoolme.framework.thrift

struct User {
    1: string username,
    2: i32 age,
}

service UserService{
	string sayHello(1:User user)
}
