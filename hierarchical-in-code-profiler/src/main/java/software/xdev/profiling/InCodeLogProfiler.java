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
package software.xdev.profiling;

import java.util.function.Consumer;


/**
 * Same as {@link InCodeProfilerAutoClosable} but with a Consumer that handles
 * {@link InCodeProfiler#getPrettyPrinted()}
 *
 * @see InCodeProfilerAutoClosable
 */
public class InCodeLogProfiler extends InCodeProfilerAutoClosable
{
	protected final Consumer<String> logConsumer;
	
	public InCodeLogProfiler(
		final String taskName,
		final Consumer<String> logConsumer,
		final boolean async,
		final boolean enabled)
	{
		super(taskName, async, enabled);
		this.logConsumer = logConsumer;
	}
	
	@Override
	public void close()
	{
		super.close();
		this.stopAll();
		if(this.isEnabled())
		{
			this.logConsumer.accept(this.getPrettyPrinted());
		}
	}
	
	public static InCodeLogProfiler createStarted(
		final String taskName,
		final Consumer<String> logConsumer,
		final boolean async,
		final boolean enabled)
	{
		final InCodeLogProfiler sw = new InCodeLogProfiler(taskName, logConsumer, async, enabled);
		sw.start();
		return sw;
	}
	
	public static InCodeLogProfiler createStarted(
		final String taskname,
		final Consumer<String> logConsumer,
		final boolean enabled)
	{
		return createStarted(taskname, logConsumer, false, enabled);
	}
}
