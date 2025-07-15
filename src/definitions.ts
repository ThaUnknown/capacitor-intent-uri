export interface IntentUriPlugin {
  openUri(options: { url: string }): Promise<{ completed: true } | { completed: false, message: string }>;
}
