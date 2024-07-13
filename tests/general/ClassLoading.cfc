component extends="org.lucee.cfml.test.LuceeTestCase" labels="ec2" {
	function run( testResults , testBox ) {
		describe( title="test if i can load an instance of a class",body=function() {
			it(title="loading instance", skip=isNotSupported(), body = function( currentSpec ) {
				var obj=createObject("java","org.lucee.extension.aws.ec2.function.EC2DescribeInstances");
				getMetaData(obj)
			});
		});
	}
}