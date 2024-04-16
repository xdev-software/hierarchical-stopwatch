package software.xdev;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import software.xdev.time.HierarchicalLoggingStopWatch;


public final class Application
{
	public static void main(final String[] args)
	{
		try(final var profiler = HierarchicalLoggingStopWatch.createStarted(
			"Run dummy",
			System.out::println, // Could also be LOGGER::debug
			true)) // Could also be LOGGER.isDebugEnabled()
		{
			final List<CompletableFuture<Void>> completableFutures;
			try(final var ignored = profiler.nestedAC("Launch tasks"))
			{
				completableFutures = IntStream.of(1, 2, 3)
					.mapToObj(i -> CompletableFuture.runAsync(() -> {
						try(final var process = profiler.nestedAC("Process " + i, true))
						{
							try(final var ignore = process.nestedAC("Fetch"))
							{
								sleep(5);
							}
							
							try(final var ignore = process.nestedAC("Process"))
							{
								sleep(i * 5);
							}
							
							if(i % 2 == 0)
							{
								try(final var ignore = process.nestedAC("Finalize"))
								{
									sleep(5);
								}
							}
						}
					}))
					.toList();
			}
			
			try(final var ignored = profiler.nestedAC("Wait for tasks"))
			{
				completableFutures.forEach(CompletableFuture::join);
			}
		}
	}
	
	private static void sleep(final int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch(final InterruptedException iex)
		{
			Thread.currentThread().interrupt();
		}
	}
	
	private Application()
	{
	}
}
