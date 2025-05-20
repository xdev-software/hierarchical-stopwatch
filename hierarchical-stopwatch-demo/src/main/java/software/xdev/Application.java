package software.xdev;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import software.xdev.time.HierarchicalLoggingStopWatch;
import software.xdev.time.HierarchicalStopWatch;


public final class Application
{
	@SuppressWarnings("java:S106")
	public static void main(final String[] args)
	{
		try(final HierarchicalStopWatch dummySw = HierarchicalLoggingStopWatch.createStarted(
			"Run dummy",
			System.out::println, // Could also be LOGGER::debug
			true)) // Could also be LOGGER.isDebugEnabled()
		{
			final List<CompletableFuture<Void>> completableFutures;
			try(final HierarchicalStopWatch ignored = dummySw.nested("Launch tasks"))
			{
				completableFutures = IntStream.of(1, 2, 3)
					.mapToObj(i -> CompletableFuture.runAsync(() -> {
						try(final var processSw = dummySw.nested("Process " + i, true))
						{
							try(final var ignore = processSw.nested("Fetch"))
							{
								sleep(5);
							}
							
							try(final var ignore = processSw.nested("Process"))
							{
								sleep(i * 5);
							}
							
							if(i % 2 == 0)
							{
								try(final var ignore = processSw.nested("Finalize"))
								{
									sleep(5);
								}
							}
						}
					}))
					.toList();
			}
			
			try(final HierarchicalStopWatch ignore = dummySw.nested("Wait for tasks"))
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
