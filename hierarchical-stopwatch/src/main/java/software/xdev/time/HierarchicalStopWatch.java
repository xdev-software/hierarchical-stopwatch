/*
 * Copyright © 2024 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;


/**
 * Presents a hierarchical StopWatch
 * <p>
 * Example:
 * <pre>
 * {@code
 * 100,00% 100,00% - getIssueInfos[keys=[X-1000]] [391ms]
 *     0,11%   0,11% - Check cache [0ms]
 *    98,78%  98,78% - Get results of async processes [386ms]
 *     ASYNC   ASYNC - Process issue[key=X-1000] async [386ms]
 *      98,70% 100,00% - getIssueInfoByKeyInternal[key=X-1000] [386ms]
 *        98,66%  99,96% - Fetch data [386ms]
 *          98,22%  99,55% - getIssueByKey[key=X-1000] [384ms]
 *           0,44%   0,45% ? unspecified [2ms]
 *         0,03%   0,03% - Wait for hierarchy-asyncs [0ms]
 *         0,00%   0,00% ? unspecified [0ms]
 *       0,00%   0,00% ? unspecified [0ms]
 *     0,38%   0,38% - Put cache [2ms]
 *     0,73%   0,73% ? unspecified [3ms]
 * }
 * </pre>
 * </p>
 */
public class HierarchicalStopWatch implements AutoCloseable
{
	protected static final double NANOS_TO_MILLIS_FACTOR = 1000000.0;
	
	protected final String taskName;
	protected final boolean async;
	protected final boolean enabled;
	
	protected final StopWatch sw = new StopWatch();
	
	protected final List<HierarchicalStopWatch> nestedProfilers = Collections.synchronizedList(new ArrayList<>());
	
	public HierarchicalStopWatch(final String taskName, final boolean async, final boolean enabled)
	{
		super();
		
		this.taskName = Objects.requireNonNull(taskName);
		this.async = async;
		this.enabled = enabled;
	}
	
	public HierarchicalStopWatch(final String taskName, final boolean async)
	{
		this(taskName, async, true);
	}
	
	public HierarchicalStopWatch(final String taskName)
	{
		this(taskName, false);
	}
	
	protected String gettaskName()
	{
		return this.taskName;
	}
	
	public StopWatch getStopWatch()
	{
		return this.sw;
	}
	
	protected boolean isEnabled()
	{
		return this.enabled;
	}
	
	protected boolean isAsync()
	{
		return this.async;
	}
	
	protected boolean isNotAsync()
	{
		return !this.isAsync();
	}
	
	public void start()
	{
		if(!this.isEnabled())
		{
			return;
		}
		this.sw.start();
	}
	
	public void stop()
	{
		if(!this.sw.isStopped())
		{
			this.sw.stop();
		}
	}
	
	@Override
	public void close()
	{
		this.stop();
	}
	
	/**
	 * Tries to stop all nested profilers
	 */
	public void stopAll()
	{
		this.stop();
		for(final HierarchicalStopWatch hsw : this.nestedProfilers)
		{
			hsw.stopAll();
		}
	}
	
	protected void addNested(final HierarchicalStopWatch nested, final boolean start)
	{
		if(!this.isEnabled())
		{
			return;
		}
		
		this.nestedProfilers.add(nested);
		
		if(start)
		{
			nested.start();
		}
	}
	
	/**
	 * Creates a new nested profiler
	 */
	public HierarchicalStopWatch nested(final String taskName, final boolean async)
	{
		final HierarchicalStopWatch nested = new HierarchicalStopWatch(taskName, async, this.isEnabled());
		this.addNested(nested, true);
		return nested;
	}
	
	/**
	 * Creates a new nested profiler
	 */
	public HierarchicalStopWatch nested(final String taskName)
	{
		return this.nested(taskName, false);
	}
	
	public String getPrettyPrinted()
	{
		if(!this.isEnabled())
		{
			return "";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator());
		sb.append("-------------------------------");
		sb.append(System.lineSeparator());
		sb.append("Root    Parent  Task");
		sb.append(System.lineSeparator());
		sb.append("-------------------------------");
		sb.append(System.lineSeparator());
		
		final long rootNanos = this.sw.getNanoTime();
		
		sb.append(new TaskEntry(
			rootNanos,
			rootNanos,
			0,
			"-",
			this.gettaskName(),
			rootNanos).format());
		this.addNestedToStrBuilder(sb, rootNanos, 1);
		
		return sb.toString();
	}
	
	protected void addNestedToStrBuilder(
		final StringBuilder sb,
		final long rootNanos,
		final int hierarchicalPosition)
	{
		final Map<String, List<HierarchicalStopWatch>> hierach =
			this.nestedProfilers.stream().collect(
				Collectors.groupingBy(
					HierarchicalStopWatch::gettaskName,
					LinkedHashMap::new,
					Collectors.toList()));
		
		final long currentNanos = this.sw.getNanoTime();
		
		long leftNanos = currentNanos;
		for(final Entry<String, List<HierarchicalStopWatch>> entry : hierach.entrySet())
		{
			final long groupMinNanos = entry.getValue()
				.stream()
				.mapToLong(hsw -> hsw.getStopWatch().getNanoTime())
				.min()
				.orElse(0);
			final long groupMaxNanos = entry.getValue()
				.stream()
				.mapToLong(hsw -> hsw.getStopWatch().getNanoTime())
				.max()
				.orElse(0);
			
			final int count = entry.getValue().size();
			
			final long groupNestedNanos = entry.getValue()
				.stream()
				.mapToLong(hsw -> hsw.getStopWatch().getNanoTime())
				.sum();
			
			sb.append(new TaskEntry(
				rootNanos,
				currentNanos,
				hierarchicalPosition,
				"-",
				entry.getKey(),
				count,
				groupMinNanos,
				groupMaxNanos,
				groupNestedNanos,
				entry.getValue().stream().anyMatch(HierarchicalStopWatch::isAsync)).format());
			
			final long groupNestedNanosNotAsync = entry.getValue()
				.stream()
				.filter(HierarchicalStopWatch::isNotAsync)
				.mapToLong(hsw -> hsw.getStopWatch().getNanoTime())
				.sum();
			leftNanos -= groupNestedNanosNotAsync;
			
			for(final HierarchicalStopWatch hsw : entry.getValue())
			{
				hsw.addNestedToStrBuilder(sb, rootNanos, hierarchicalPosition + 1);
			}
		}
		
		if(!hierach.isEmpty())
		{
			sb.append(new TaskEntry(
				rootNanos,
				currentNanos,
				hierarchicalPosition,
				"?",
				"unspecified",
				leftNanos).format());
		}
	}
	
	public static HierarchicalStopWatch createStarted(final String taskName)
	{
		final HierarchicalStopWatch sw = new HierarchicalStopWatch(taskName);
		sw.start();
		return sw;
	}
	
	protected record TaskEntry(
		long rootNanos,
		long parentNanos,
		int hierarchicalPosition,
		String delimiter,
		String taskName,
		long count,
		long minNanos,
		long maxNanos,
		long currentNanos,
		boolean async
	)
	{
		public TaskEntry(
			final long rootNanos,
			final long parentNanos,
			final int hierarchicalPosition,
			final String delimiter,
			final String taskName,
			final long currentNanos)
		{
			this(
				rootNanos,
				parentNanos,
				hierarchicalPosition,
				delimiter,
				taskName,
				0,
				0,
				0,
				currentNanos,
				false
			);
		}
		
		public String format()
		{
			final String percentFormat = "%6.2f%%";
			final String asyncPercent = "  ASYNC";
			
			final String multipleInfo = this.count() <= 1
				? ""
				: String.format(
				"; %dx; min=%.0fms; max=%.0fms",
				this.count(),
				this.minNanos() / NANOS_TO_MILLIS_FACTOR,
				this.maxNanos() / NANOS_TO_MILLIS_FACTOR);
			
			final double currentNanosDouble = this.currentNanos();
			return String.format(
				"%s%s %s %s %s [%.0fms%s]%s",
				"  ".repeat(this.hierarchicalPosition()),
				this.async()
					? asyncPercent
					: String.format(percentFormat, currentNanosDouble / this.rootNanos() * 100.0),
				this.async()
					? asyncPercent
					: String.format(percentFormat, currentNanosDouble / this.parentNanos() * 100.0),
				this.delimiter(),
				this.taskName(),
				currentNanosDouble / NANOS_TO_MILLIS_FACTOR,
				multipleInfo,
				System.lineSeparator());
		}
	}
}
