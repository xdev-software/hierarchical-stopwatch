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

/**
 * Same as {@link HierarchicalStopWatch} but also implements {@link AutoCloseable}
 *
 * @see HierarchicalStopWatch
 */
public class HierarchicalStopWatchAutoClosable extends HierarchicalStopWatch implements AutoCloseable
{
	public HierarchicalStopWatchAutoClosable(final String taskName)
	{
		super(taskName);
	}
	
	public HierarchicalStopWatchAutoClosable(final String taskName, final boolean async)
	{
		super(taskName, async);
	}
	
	public HierarchicalStopWatchAutoClosable(final String taskName, final boolean async, final boolean enabled)
	{
		super(taskName, async, enabled);
	}
	
	@Override
	public void close()
	{
		this.stop();
	}
	
	public static HierarchicalStopWatchAutoClosable createStarted(final String taskName)
	{
		final HierarchicalStopWatchAutoClosable sw = new HierarchicalStopWatchAutoClosable(taskName);
		sw.start();
		return sw;
	}
}
