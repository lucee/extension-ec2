package org.lucee.extension.aws.ec2.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.lucee.extension.aws.ec2.AmazonEC2Client;
import org.lucee.extension.aws.ec2.util.BeanUtil;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;

import lucee.commons.io.log.Log;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;

public class EC2DescribeInstances extends EC2Function {

	private static final long serialVersionUID = 5961249624495798999L;

	public static Struct call(PageContext pc, String accessKeyId, String secretAccessKey, Array instanceIds, Struct filters, String host, String location, double timeout)
			throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();

		// for backward compatibility, when host was not existing
		if (eng.getDecisionUtil().isNumber(host)) {
			timeout = eng.getCastUtil().toDoubleValue(host);
			host = null;
		}

		if (eng.getStringUtil().isEmpty(location, true)) location = null;
		boolean legacyMode = false;
		try {
			Log log = pc.getConfig().getLog("application");

			AmazonEC2 client = AmazonEC2Client.get(accessKeyId, secretAccessKey, host, location, toTimeout(timeout), log);
			DescribeInstancesRequest req = new DescribeInstancesRequest();
			if (instanceIds != null && instanceIds.size() > 0) req.setInstanceIds(toCollection(instanceIds));

			if (filters != null && filters.size() > 0) {
				legacyMode = eng.getCastUtil().toBooleanValue(filters.remove("legacy-mode"), false);

				if (filters != null) setFilter(pc, req, filters);
			}
			DescribeInstancesResult dis = client.describeInstances(req);
			Struct result = (Struct) BeanUtil.beanToCFML(dis, legacyMode);

			result.set("raw", dis.toString());
			result.set("legacyMode", legacyMode);
			return result;
		}
		catch (Exception e) {
			throw eng.getCastUtil().toPageException(e);
		}
	}

	private static void setFilter(PageContext pc, DescribeInstancesRequest req, Struct filters) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		List<Filter> list = new ArrayList<>();
		Iterator<Entry<Key, Object>> it = filters.entryIterator();
		Entry<Key, Object> e;
		Object val;
		Array arr;
		Cast caster = eng.getCastUtil();
		List<String> values = new ArrayList<>();
		while (it.hasNext()) {
			e = it.next();
			val = e.getValue();
			if (eng.getDecisionUtil().isCastableToArray(val)) {
				arr = caster.toArray(val);
				Iterator<Object> itt = arr.valueIterator();
				while (itt.hasNext()) {
					values.add(caster.toString(itt.next()));
				}
			}
			else {
				values.add(caster.toString(val));
			}
			list.add(new Filter(e.getKey().getString(), values));
		}
		req.withFilters(list);

		// .withFilters(new Filter("private-ip-address", ips)

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		Cast cast = engine.getCastUtil();

		if (args.length == 7) {
			return call(pc, cast.toString(args[0]), cast.toString(args[1]), args[2] == null ? null : cast.toArray(args[2]), args[3] == null ? null : cast.toStruct(args[3]),
					cast.toString(args[4]), cast.toString(args[5]), cast.toDoubleValue(args[6]));
		}
		if (args.length == 6) {
			return call(pc, cast.toString(args[0]), cast.toString(args[1]), args[2] == null ? null : cast.toArray(args[2]), args[3] == null ? null : cast.toStruct(args[3]),
					cast.toString(args[4]), cast.toString(args[5]), 0);
		}
		if (args.length == 5) {
			return call(pc, cast.toString(args[0]), cast.toString(args[1]), args[2] == null ? null : cast.toArray(args[2]), args[3] == null ? null : cast.toStruct(args[3]),
					cast.toString(args[4]), null, 0);
		}
		if (args.length == 4) {
			return call(pc, cast.toString(args[0]), cast.toString(args[1]), args[2] == null ? null : cast.toArray(args[2]), args[3] == null ? null : cast.toStruct(args[3]), null,
					null, 0);
		}
		if (args.length == 3) {
			return call(pc, cast.toString(args[0]), cast.toString(args[1]), args[2] == null ? null : cast.toArray(args[2]), null, null, null, 0);
		}
		if (args.length == 2) {
			return call(pc, cast.toString(args[0]), cast.toString(args[1]), null, null, null, null, 0);
		}

		throw engine.getExceptionUtil().createFunctionException(pc, "EC2DescribeInstances", 2, 7, args.length);
	}
}